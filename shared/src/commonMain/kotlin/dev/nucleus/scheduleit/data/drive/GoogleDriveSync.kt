package dev.nucleus.scheduleit.data.drive

import kotlinx.coroutines.flow.StateFlow

interface GoogleDriveSync {
    val status: StateFlow<GoogleDriveStatus>
    suspend fun connect()
    suspend fun disconnect()
    suspend fun backup(payload: String)
    /** Returns the JSON payload of the latest backup, or null if none exists. */
    suspend fun restore(): String?
}

sealed interface GoogleDriveStatus {
    data object Disconnected : GoogleDriveStatus
    data object Connecting : GoogleDriveStatus
    data class Connected(
        val email: String?,
        val lastBackupEpochSec: Long?,
        val operation: Operation? = null,
    ) : GoogleDriveStatus
    data class Error(val message: String) : GoogleDriveStatus

    enum class Operation { Uploading, Restoring }
}

expect fun createGoogleDriveSync(): GoogleDriveSync?
