package dev.nucleus.scheduleit

import io.github.kdroidfilter.nucleus.nativehttp.NativeHttpClient
import io.github.kdroidfilter.nucleus.updater.NucleusUpdater
import io.github.kdroidfilter.nucleus.updater.UpdateInfo
import io.github.kdroidfilter.nucleus.updater.UpdateResult
import io.github.kdroidfilter.nucleus.updater.provider.GitHubProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Downloading(val percent: Double, val newVersion: String) : UpdateState
    data class ReadyToInstall(val file: File, val newVersion: String) : UpdateState
    data object UpToDate : UpdateState
    data class Failed(val message: String) : UpdateState
}

class AppUpdater {
    private val updater = NucleusUpdater {
        provider = GitHubProvider(owner = "kdroidFilter", repo = "ScheduleIt")
        httpClient = NativeHttpClient.create()
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    fun start(scope: CoroutineScope) {
        if (!updater.isUpdateSupported()) return
        scope.launch(Dispatchers.IO) {
            _state.value = UpdateState.Checking
            when (val result = updater.checkForUpdates()) {
                is UpdateResult.Available -> downloadInBackground(result.info.version, result.info)
                UpdateResult.NotAvailable -> _state.value = UpdateState.UpToDate
                is UpdateResult.Error -> _state.value = UpdateState.Failed(
                    result.exception.message ?: "Update check failed",
                )
            }
        }
    }

    fun installAndRestart() {
        val ready = _state.value as? UpdateState.ReadyToInstall ?: return
        updater.installAndRestart(ready.file)
    }

    fun installAndQuit() {
        val ready = _state.value as? UpdateState.ReadyToInstall ?: return
        updater.installAndQuit(ready.file)
    }

    private suspend fun downloadInBackground(newVersion: String, info: UpdateInfo) {
        _state.value = UpdateState.Downloading(0.0, newVersion)
        runCatching {
            updater.downloadUpdate(info).collect { progress ->
                val file = progress.file
                _state.value = if (file != null) {
                    UpdateState.ReadyToInstall(file, newVersion)
                } else {
                    UpdateState.Downloading(progress.percent, newVersion)
                }
            }
        }.onFailure {
            _state.value = UpdateState.Failed(it.message ?: "Download failed")
        }
    }
}
