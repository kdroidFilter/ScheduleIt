package dev.nucleus.scheduleit.data.drive

actual fun createGoogleDriveSync(): GoogleDriveSync? =
    if (DriveOAuthConfig.isConfigured) JvmGoogleDriveSync() else null
