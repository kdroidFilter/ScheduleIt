package dev.nucleus.scheduleit

import io.github.kdroidfilter.nucleus.notification.AuthorizationOption
import io.github.kdroidfilter.nucleus.notification.NotificationCenter
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

fun requestMacosNotificationAuthorizationIfNeeded() {
    if (hostOs != OS.MacOS) return
    runCatching {
        NotificationCenter.requestAuthorization(
            setOf(
                AuthorizationOption.ALERT,
                AuthorizationOption.SOUND,
                AuthorizationOption.BADGE,
            ),
        ) { _, _ -> /* ignore — system already shows the dialog once */ }
    }
}
