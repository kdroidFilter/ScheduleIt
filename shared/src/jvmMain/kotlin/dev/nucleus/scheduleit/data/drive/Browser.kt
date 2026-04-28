package dev.nucleus.scheduleit.data.drive

import java.awt.Desktop
import java.net.URI

internal object Browser {
    fun open(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            val ok = runCatching { Desktop.getDesktop().browse(URI(url)) }.isSuccess
            if (ok) return
        }
        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("win") -> arrayOf("rundll32", "url.dll,FileProtocolHandler", url)
            os.contains("mac") -> arrayOf("open", url)
            else -> arrayOf("xdg-open", url)
        }
        ProcessBuilder(*cmd).start()
    }
}
