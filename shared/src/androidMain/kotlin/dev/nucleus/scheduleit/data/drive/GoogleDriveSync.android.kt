package dev.nucleus.scheduleit.data.drive

actual fun createGoogleDriveSync(): GoogleDriveSync? {
    val context = AndroidContextHolder.applicationContext ?: return null
    return AndroidGoogleDriveSync(context)
}
