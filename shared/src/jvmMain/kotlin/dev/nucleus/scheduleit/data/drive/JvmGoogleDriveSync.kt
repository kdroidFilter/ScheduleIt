package dev.nucleus.scheduleit.data.drive

import io.github.kdroidfilter.nucleus.nativehttp.ktor.installNativeSsl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.future.await

internal class JvmGoogleDriveSync(
    private val store: TokenStore = TokenStore(),
    private val httpClient: HttpClient = HttpClient(CIO) { installNativeSsl() },
    clientId: String = DriveOAuthConfig.CLIENT_ID,
    clientSecret: String = DriveOAuthConfig.CLIENT_SECRET,
) : GoogleDriveSync {

    private val oauth = GoogleOAuthClient(httpClient, clientId, clientSecret)
    private val drive = DriveBackup(httpClient)

    private val _status = MutableStateFlow<GoogleDriveStatus>(initialStatus())
    override val status: StateFlow<GoogleDriveStatus> = _status.asStateFlow()

    private fun initialStatus(): GoogleDriveStatus {
        val saved = store.load() ?: return GoogleDriveStatus.Disconnected
        return GoogleDriveStatus.Connected(
            email = saved.email,
            lastBackupEpochSec = saved.lastBackupEpochSec,
        )
    }

    override suspend fun connect() {
        if (_status.value is GoogleDriveStatus.Connecting) return
        _status.value = GoogleDriveStatus.Connecting
        val verifier = Pkce.newVerifier()
        val challenge = Pkce.challenge(verifier)
        val state = Pkce.newVerifier().take(32)

        val receiver = LoopbackCodeReceiver().start(state)
        try {
            val redirectUri = "http://127.0.0.1:${receiver.port}/callback"
            Browser.open(buildAuthUrl(redirectUri, state, challenge))

            val code = receiver.result.orTimeout(5, TimeUnit.MINUTES).await()
            val tokens = oauth.exchangeCode(code, verifier, redirectUri)
            val refresh = tokens.refresh_token
                ?: error("No refresh_token returned. Make sure prompt=consent is set.")
            val email = oauth.fetchUserEmail(tokens.access_token)
            val now = nowEpochSec()
            store.save(
                StoredTokens(
                    accessToken = tokens.access_token,
                    refreshToken = refresh,
                    expiresAtEpochSec = now + tokens.expires_in - 60,
                    email = email,
                ),
            )
            _status.value = GoogleDriveStatus.Connected(email = email, lastBackupEpochSec = null)
        } catch (t: Throwable) {
            _status.value = GoogleDriveStatus.Error(t.message ?: "Connection failed")
        } finally {
            receiver.close()
        }
    }

    override suspend fun disconnect() {
        store.clear()
        _status.value = GoogleDriveStatus.Disconnected
    }

    override suspend fun backup(payload: String) {
        val current = _status.value
        if (current !is GoogleDriveStatus.Connected) return
        val saved = store.load() ?: run {
            _status.value = GoogleDriveStatus.Disconnected
            return
        }
        _status.value = current.copy(operation = GoogleDriveStatus.Operation.Uploading)
        try {
            val fresh = ensureFreshToken(saved)
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val fileId = fresh.backupFileId ?: drive.create(fresh.accessToken, BACKUP_FILE_NAME, bytes)
                .also { newId -> store.save(fresh.copy(backupFileId = newId)) }
            if (fresh.backupFileId != null) {
                drive.update(fresh.accessToken, fileId, bytes)
            }
            val now = nowEpochSec()
            store.save(store.load()!!.copy(lastBackupEpochSec = now))
            _status.value = GoogleDriveStatus.Connected(
                email = fresh.email,
                lastBackupEpochSec = now,
                operation = null,
            )
        } catch (t: Throwable) {
            _status.value = GoogleDriveStatus.Error(t.message ?: "Backup failed")
        }
    }

    override suspend fun restore(): String? {
        val current = _status.value
        if (current !is GoogleDriveStatus.Connected) return null
        val saved = store.load() ?: run {
            _status.value = GoogleDriveStatus.Disconnected
            return null
        }
        _status.value = current.copy(operation = GoogleDriveStatus.Operation.Restoring)
        return try {
            val fresh = ensureFreshToken(saved)
            val ref = drive.findLatest(fresh.accessToken, BACKUP_FILE_NAME) ?: run {
                _status.value = current.copy(operation = null)
                return null
            }
            val payload = drive.download(fresh.accessToken, ref.id)
            store.save(
                fresh.copy(
                    backupFileId = ref.id,
                    lastBackupEpochSec = ref.modifiedEpochSec,
                ),
            )
            _status.value = GoogleDriveStatus.Connected(
                email = fresh.email,
                lastBackupEpochSec = ref.modifiedEpochSec,
                operation = null,
            )
            payload
        } catch (t: Throwable) {
            _status.value = GoogleDriveStatus.Error(t.message ?: "Restore failed")
            null
        }
    }

    private suspend fun ensureFreshToken(saved: StoredTokens): StoredTokens {
        val now = nowEpochSec()
        if (now < saved.expiresAtEpochSec) return saved
        val refreshed = oauth.refresh(saved.refreshToken)
        val updated = saved.copy(
            accessToken = refreshed.access_token,
            refreshToken = refreshed.refresh_token ?: saved.refreshToken,
            expiresAtEpochSec = now + refreshed.expires_in - 60,
        )
        store.save(updated)
        return updated
    }

    private fun buildAuthUrl(redirectUri: String, state: String, challenge: String): String {
        val params = linkedMapOf(
            "client_id" to DriveOAuthConfig.CLIENT_ID,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to SCOPES,
            "code_challenge" to challenge,
            "code_challenge_method" to "S256",
            "state" to state,
            "access_type" to "offline",
            "prompt" to "consent",
        )
        val query = params.entries.joinToString("&") { (k, v) ->
            "$k=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "$AUTH_URL?$query"
    }

    private fun nowEpochSec(): Long = System.currentTimeMillis() / 1000

    companion object {
        private const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val SCOPES =
            "https://www.googleapis.com/auth/drive.appdata openid email"
        private const val BACKUP_FILE_NAME = "scheduleit-backup.json"
    }
}
