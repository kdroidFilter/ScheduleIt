package dev.nucleus.scheduleit.data.drive

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Android implementation of [GoogleDriveSync] using:
 *  - Google Identity `AuthorizationClient` for the consent flow
 *  - `GoogleAuthUtil.getToken` to obtain a fresh OAuth access token for the
 *    Drive `appdata` scope from the device's Google account
 *  - DataStore-backed [AndroidTokenStore] for persistence
 *  - Ktor + [AndroidDriveBackup] for the REST calls
 *
 * Mirrors the behaviour of [dev.nucleus.scheduleit.data.drive.JvmGoogleDriveSync]
 * (same status flow, same backup file name) so [ScheduleViewModel] works
 * identically across desktop and Android.
 */
internal class AndroidGoogleDriveSync(
    private val context: Context,
) : GoogleDriveSync {

    private val store = AndroidTokenStore(context)
    private val http: HttpClient = HttpClient(CIO)
    private val drive = AndroidDriveBackup(http)
    private val json = Json { ignoreUnknownKeys = true }
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _status = MutableStateFlow<GoogleDriveStatus>(GoogleDriveStatus.Disconnected)
    override val status: StateFlow<GoogleDriveStatus> = _status.asStateFlow()

    init {
        // Hydrate from persisted state so the connected status survives app
        // restarts. The access token may be stale; backup/restore will call
        // [ensureFreshToken] which silently re-authorises via Identity (no UI
        // if the user previously granted the scope).
        ioScope.launch {
            val saved = store.load() ?: return@launch
            _status.value = GoogleDriveStatus.Connected(
                email = saved.email,
                lastBackupEpochSec = saved.lastBackupEpochSec,
            )
        }
    }

    override suspend fun connect() {
        if (_status.value is GoogleDriveStatus.Connecting) return
        _status.value = GoogleDriveStatus.Connecting
        try {
            val token = authorizeAndGetToken(allowResolution = true) ?: run {
                _status.value = GoogleDriveStatus.Error("Authorization cancelled")
                return
            }
            // Best-effort email lookup via the OAuth userinfo endpoint. Falls
            // back to null if the email/profile scopes weren't granted.
            val email = runCatching { fetchUserEmail(token) }.getOrNull()
            val now = nowEpochSec()
            store.save(
                StoredAndroidTokens(
                    accountName = email ?: "google-account",
                    email = email,
                    accessToken = token,
                    expiresAtEpochSec = now + DEFAULT_TOKEN_TTL_SEC,
                    backupFileId = null,
                    lastBackupEpochSec = null,
                ),
            )
            _status.value = GoogleDriveStatus.Connected(
                email = email,
                lastBackupEpochSec = null,
            )
        } catch (t: Throwable) {
            _status.value = GoogleDriveStatus.Error(t.message ?: "Connection failed")
        }
    }

    /**
     * Runs the Authorization flow and returns the granted access token, or null
     * if the user cancels. When [allowResolution] is false, only the silent path
     * is attempted and we return null whenever consent UI would be required —
     * useful for refreshing an already-granted token without showing UI again.
     */
    private suspend fun authorizeAndGetToken(allowResolution: Boolean): String? {
        val request = AuthorizationRequest.Builder()
            .setRequestedScopes(
                listOf(
                    Scope(SCOPE_DRIVE_APPDATA),
                    Scope("email"),
                    Scope("profile"),
                ),
            )
            .build()
        val client = Identity.getAuthorizationClient(context)
        var result = client.authorize(request).await()
        if (result.hasResolution()) {
            if (!allowResolution) return null
            val sender = result.pendingIntent?.intentSender ?: return null
            val resolver = DriveAuthResolverHolder.resolver ?: return null
            val data = resolver.resolve(sender) ?: return null
            result = client.getAuthorizationResultFromIntent(data)
        }
        return result.accessToken
    }

    private suspend fun fetchUserEmail(token: String): String? {
        val response = http.get(USERINFO_URL) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (!response.status.isSuccess()) return null
        return json.parseToJsonElement(response.bodyAsText())
            .jsonObject["email"]?.jsonPrimitive?.content
    }

    override suspend fun disconnect() {
        // Wipe local state. The Drive scope itself stays granted in the user's
        // Google account until they revoke it manually at
        // https://myaccount.google.com/permissions; the next connect just
        // re-runs through Identity which is silent if scopes still hold.
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
            val fileId = fresh.backupFileId
                ?: drive.create(fresh.accessToken, BACKUP_FILE_NAME, bytes).also { newId ->
                    store.save(fresh.copy(backupFileId = newId))
                }
            if (fresh.backupFileId != null) {
                drive.update(fresh.accessToken, fileId, bytes)
            }
            val now = nowEpochSec()
            val updated = (store.load() ?: fresh).copy(lastBackupEpochSec = now)
            store.save(updated)
            _status.value = GoogleDriveStatus.Connected(
                email = updated.email,
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

    /**
     * Returns a non-expired access token. Re-runs the Authorization flow when
     * past expiry — silent first (no UI), falling back to a resolution if the
     * user has revoked consent in the meantime.
     */
    private suspend fun ensureFreshToken(saved: StoredAndroidTokens): StoredAndroidTokens {
        val now = nowEpochSec()
        if (now < saved.expiresAtEpochSec) return saved
        val newToken = authorizeAndGetToken(allowResolution = false)
            ?: authorizeAndGetToken(allowResolution = true)
            ?: error("Re-authorization required")
        val updated = saved.copy(
            accessToken = newToken,
            expiresAtEpochSec = nowEpochSec() + DEFAULT_TOKEN_TTL_SEC,
        )
        store.save(updated)
        return updated
    }

    private fun nowEpochSec(): Long = System.currentTimeMillis() / 1000

    companion object {
        private const val SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
        private const val USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
        private const val BACKUP_FILE_NAME = "scheduleit-backup.json"
        private const val DEFAULT_TOKEN_TTL_SEC = 50L * 60L // ~50 min, refresh just before 1h expiry
    }
}
