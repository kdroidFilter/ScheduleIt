package dev.nucleus.scheduleit.data.drive

import java.io.File

internal object AppDataDir {
    private const val APP_NAME = "ScheduleIt"

    fun resolve(): File {
        val os = System.getProperty("os.name").lowercase()
        val home = System.getProperty("user.home")
        val dir = when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")?.takeIf { it.isNotEmpty() } ?: "$home/AppData/Roaming"
                File(appData, APP_NAME)
            }
            os.contains("mac") -> File(home, "Library/Application Support/$APP_NAME")
            else -> {
                val xdg = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotEmpty() } ?: "$home/.local/share"
                File(xdg, APP_NAME.lowercase())
            }
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
