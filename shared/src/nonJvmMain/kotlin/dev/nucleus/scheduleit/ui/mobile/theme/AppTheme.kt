package dev.nucleus.scheduleit.ui.mobile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalMobileColors = compositionLocalOf<MobileColors> {
    error("MobileColors not provided. Wrap your UI in MobileTheme { }.")
}

private val LocalMobileTypography = staticCompositionLocalOf { DefaultTypography }

object MobileTheme {
    val colors: MobileColors
        @Composable @ReadOnlyComposable
        get() = LocalMobileColors.current

    val typography: MobileTypography
        @Composable @ReadOnlyComposable
        get() = LocalMobileTypography.current
}

@Composable
fun MobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typography: MobileTypography = DefaultTypography,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalMobileColors provides colors,
        LocalMobileTypography provides typography,
        content = content,
    )
}
