# ScheduleIt ProGuard rules (inspired by Nucleus jewel-sample)

-keepclasseswithmembers public class dev.nucleus.scheduleit.MainKt {
    public static void main(java.lang.String[]);
}

-dontwarn kotlinx.coroutines.debug.*

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

# JNA (transitively used by Nucleus native modules)
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }
-keepclassmembers class * implements com.sun.jna.* { public *; }
-keep class com.sun.jna.platform.** { *; }
-keep class com.sun.jna.win32.** { *; }
-dontwarn com.sun.jna.**
-dontnote com.sun.jna**

-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

# kotlinx.serialization
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# SQLDelight + SQLite JDBC driver (loaded via DriverManager)
-keep class org.sqlite.** { *; }
-keep class * implements java.sql.Driver { *; }
-dontwarn org.sqlite.**

# FileKit (file/folder pickers — loaded reflectively / via ServiceLoader)
-keep class io.github.vinceglb.filekit.** { *; }
-dontwarn io.github.vinceglb.filekit.**

# SLF4J (transitive)
-dontwarn org.slf4j.**
-dontwarn javax.naming.**

# Don't obfuscate (matches Nucleus sample)
-dontobfuscate
-ignorewarnings

-keep class sun.misc.Unsafe { *; }
-dontnote sun.misc.Unsafe

# JetBrains Runtime APIs (JBR-specific extensions used by Jewel/decorated window)
-keep class com.jetbrains.JBR* { *; }
-dontnote com.jetbrains.JBR*
-keep class com.jetbrains.** { *; }
-dontwarn com.jetbrains.**
-dontnote com.jetbrains.**

-keep class androidx.compose.ui.input.key.KeyEvent_desktopKt { *; }
-dontnote androidx.compose.ui.input.key.KeyEvent_desktopKt
-keep class androidx.compose.ui.input.key.KeyEvent_skikoKt { *; }
-dontnote androidx.compose.ui.input.key.KeyEvent_skikoKt
-dontwarn androidx.compose.ui.input.key.KeyEvent_skikoKt

# Jewel — preserve sealed PainterHint hierarchy
-keepattributes PermittedSubclasses
-keep class org.jetbrains.jewel.ui.painter.** { *; }
-dontwarn org.jetbrains.jewel.ui.painter.**
-dontnote org.jetbrains.jewel.foundation.lazy.**
-dontwarn org.jetbrains.jewel.foundation.lazy.**
-dontnote org.jetbrains.jewel.foundation.util.**
-dontwarn org.jetbrains.jewel.foundation.util.**

# =============================================================================
# Nucleus JNI keep rules — kept manually to be safe across plugin versions
# =============================================================================

# decorated-window JNI (macOS + cross-platform)
-keep class io.github.kdroidfilter.nucleus.window.utils.macos.NativeMacBridge {
    native <methods>;
}
-keep class io.github.kdroidfilter.nucleus.window.** { *; }

# darkmode-detector JNI (macOS / Linux / Windows)
-keep class io.github.kdroidfilter.nucleus.darkmodedetector.mac.NativeDarkModeBridge {
    native <methods>;
    static void onThemeChanged(boolean);
}
-keep class io.github.kdroidfilter.nucleus.darkmodedetector.linux.NativeLinuxBridge {
    native <methods>;
    static void onThemeChanged(boolean);
}
-keep class io.github.kdroidfilter.nucleus.darkmodedetector.windows.NativeWindowsBridge {
    native <methods>;
}
-keep class io.github.kdroidfilter.nucleus.darkmodedetector.** { *; }

# Nucleus modules used by ScheduleIt (notifications, scheduler, menu-macos, runtimes)
-keep class io.github.kdroidfilter.nucleus.notification.** { *; }
-keep class io.github.kdroidfilter.nucleus.scheduler.** { *; }
-keep class io.github.kdroidfilter.nucleus.menu.macos.** { *; }
-keep class io.github.kdroidfilter.nucleus.core.runtime.** { *; }
-keep class io.github.kdroidfilter.nucleus.aot.runtime.** { *; }
-keep class io.github.kdroidfilter.nucleus.graalvm.** { *; }

# Metro DI — keep generated graphs / factories (often referenced reflectively)
-keep class dev.zacsweers.metrox.** { *; }
-keep @dev.zacsweers.metro.** class * { *; }
-dontwarn dev.zacsweers.metro.**

# =============================================================================
# Google Drive backup — Ktor + CIO engine + Nucleus native HTTP SSL
# =============================================================================

# Ktor (HTTP client). Many internals resolved via ServiceLoader / reflection.
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# CIO engine factory is discovered through META-INF/services.
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }
-keep class io.ktor.client.engine.cio.** { *; }

# Nucleus native HTTP / native trust store (JNI + SSL).
-keep class io.github.kdroidfilter.nucleus.nativehttp.** { *; }
-keepclassmembers class io.github.kdroidfilter.nucleus.nativehttp.** { *; }
-dontwarn io.github.kdroidfilter.nucleus.nativehttp.**

# atomicfu / SLF4J pulled in transitively by Ktor — silence warnings.
-dontwarn kotlinx.atomicfu.**
-dontwarn javax.servlet.**
-dontwarn javax.net.ssl.**
