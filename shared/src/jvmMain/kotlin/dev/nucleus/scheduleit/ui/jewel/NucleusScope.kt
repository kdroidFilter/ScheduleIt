package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.runtime.staticCompositionLocalOf
import dev.nucleusframework.application.NucleusApplicationScope

/**
 * Exposes the [NucleusApplicationScope] created by nucleusApplication to deeply nested
 * composables. Decorated dialogs must be opened through the scope-based overload so they
 * use the active backend (Tao); the plain AWT overload is unavailable on the Tao backend.
 */
val LocalNucleusApplicationScope = staticCompositionLocalOf<NucleusApplicationScope> {
    error("LocalNucleusApplicationScope not provided — wrap the window content in a provider")
}
