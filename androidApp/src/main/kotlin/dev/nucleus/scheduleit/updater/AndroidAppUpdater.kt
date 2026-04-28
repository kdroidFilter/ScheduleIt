package dev.nucleus.scheduleit.updater

import android.content.Context
import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.solrudev.ackpine.DelicateAckpineApi
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.installer.parameters.InstallerType
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.Confirmation

private const val GITHUB_OWNER = "kdroidFilter"
private const val GITHUB_REPO = "ScheduleIt"
private const val ASSET_PREFIX = "ScheduleIt-"
private const val ASSET_SUFFIX = ".apk"

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Downloading(val percent: Double, val newVersion: String) : UpdateState
    data class ReadyToInstall(val file: File, val newVersion: String) : UpdateState
    data object Installing : UpdateState
    data object UpToDate : UpdateState
    data class Failed(val message: String) : UpdateState
}

/**
 * Mirror of the desktop AppUpdater for Android: polls the latest GitHub
 * release, downloads the APK asset and installs it via Ackpine.
 *
 * Silent install relies on `requireUserAction = false` (mandatory for Ackpine
 * to skip the confirmation prompt) plus `UPDATE_PACKAGES_WITHOUT_USER_ACTION`
 * in the manifest. On API 31+ the system honours it only when the app is the
 * update owner of the previously installed version, so the very first
 * sideload still surfaces the system "Install?" dialog.
 */
class AndroidAppUpdater(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val http = HttpClient(OkHttp)
    private val packageInstaller by lazy { PackageInstaller.getInstance(context) }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            _state.value = UpdateState.Checking
            runCatching { check() }.onFailure {
                _state.value = UpdateState.Failed(it.message ?: "Update check failed")
            }
        }
    }

    fun install(scope: CoroutineScope) {
        val ready = _state.value as? UpdateState.ReadyToInstall ?: return
        scope.launch(Dispatchers.IO) {
            _state.value = UpdateState.Installing
            runCatching { runInstall(ready) }.onFailure {
                _state.value = UpdateState.Failed(it.message ?: "Install failed")
            }
        }
    }

    private suspend fun check() {
        val release = fetchLatestRelease()
        val newVersion = release.tagName.removePrefix("v")
        if (!isNewer(newVersion, currentVersion())) {
            _state.value = UpdateState.UpToDate
            return
        }
        val asset = release.assets.firstOrNull {
            it.name.startsWith(ASSET_PREFIX) && it.name.endsWith(ASSET_SUFFIX)
        } ?: error("No APK asset on release ${release.tagName}")
        val file = downloadApk(asset.url, newVersion)
        _state.value = UpdateState.ReadyToInstall(file, newVersion)
    }

    @OptIn(DelicateAckpineApi::class)
    private suspend fun runInstall(ready: UpdateState.ReadyToInstall) {
        val session = packageInstaller.createSession(Uri.fromFile(ready.file)) {
            installerType = InstallerType.SESSION_BASED
            confirmation = Confirmation.IMMEDIATE
            // Mandatory for Ackpine to skip the user prompt — combined with
            // UPDATE_PACKAGES_WITHOUT_USER_ACTION the system performs the
            // install silently when this app is the update owner.
            requireUserAction = false
            // API 34+: claim update ownership during this install. The first
            // sideload from GitHub leaves Chrome as installer, so the initial
            // self-update still surfaces the system dialog; afterwards
            // ScheduleIt owns updates and they go through silently.
            requestUpdateOwnership = true
            name = "ScheduleIt ${ready.newVersion}"
        }
        when (val terminal = session.await()) {
            is Session.State.Succeeded -> Unit
            is Session.State.Failed -> _state.value =
                UpdateState.Failed(terminal.failure.toString())
        }
    }

    private suspend fun fetchLatestRelease(): GitHubRelease {
        val body = http.get(
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest",
        ) {
            header(HttpHeaders.Accept, "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
        }.bodyAsText()
        return json.decodeFromString(GitHubRelease.serializer(), body)
    }

    private suspend fun downloadApk(url: String, version: String): File {
        val outDir = File(context.cacheDir, "updates").apply { mkdirs() }
        outDir.listFiles()?.forEach { it.delete() }
        val outFile = File(outDir, "ScheduleIt-$version.apk")
        val response: HttpResponse = http.get(url)
        val total = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1L
        val channel = response.bodyAsChannel()
        var written = 0L
        val buffer = ByteArray(64 * 1024)
        outFile.outputStream().use { out ->
            while (!channel.isClosedForRead) {
                val read = channel.readAvailable(buffer, 0, buffer.size)
                if (read <= 0) break
                out.write(buffer, 0, read)
                written += read
                if (total > 0) {
                    _state.value = UpdateState.Downloading(
                        percent = (written.toDouble() / total) * 100.0,
                        newVersion = version,
                    )
                }
            }
        }
        return outFile
    }

    private fun currentVersion(): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"

    private fun isNewer(remote: String, current: String): Boolean {
        val r = remote.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val c = current.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val len = maxOf(r.size, c.size)
        for (i in 0 until len) {
            val ri = r.getOrElse(i) { 0 }
            val ci = c.getOrElse(i) { 0 }
            if (ri != ci) return ri > ci
        }
        return false
    }
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
private data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val url: String,
)
