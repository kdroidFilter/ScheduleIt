# R8 / ProGuard rules for ScheduleIt release builds.
# Combined with proguard-android-optimize.txt for default Android shrinking +
# code optimisation. Keep this file minimal — most libraries ship their own
# consumer-rules.pro that R8 picks up automatically.

# ── kotlinx.serialization ────────────────────────────────────────────────────
# Keep generated $$serializer companions and KSerializer factories so the
# reflection-free serialization runtime can locate them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class dev.nucleus.scheduleit.data.**$$serializer { *; }
-keepclassmembers class dev.nucleus.scheduleit.data.** {
    *** Companion;
}
-keepclasseswithmembers class dev.nucleus.scheduleit.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Same for any other @Serializable in our packages.
-keep,includedescriptorclasses class dev.nucleus.scheduleit.**$$serializer { *; }
-keepclassmembers class dev.nucleus.scheduleit.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Metro DI (zacsweers/metro) ───────────────────────────────────────────────
# Generated graph implementations are referenced via reflection by createGraphFactory.
-keep class dev.nucleus.scheduleit.di.** { *; }
-keep class dev.zacsweers.metro.** { *; }
-keep class dev.zacsweers.metrox.** { *; }
-keep @dev.zacsweers.metro.DependencyGraph class * { *; }
-keep @dev.zacsweers.metro.Inject class * { *; }
-keep @dev.zacsweers.metro.ContributesIntoMap class * { *; }
-keep @dev.zacsweers.metro.ContributesBinding class * { *; }

# ── Compose Multiplatform ────────────────────────────────────────────────────
# Compose composables are usually safe with default rules but resource accessors
# (org.jetbrains.compose.resources) read fields by name.
-keep class scheduleit.shared.generated.resources.** { *; }
-keep class * implements org.jetbrains.compose.resources.ResourceItemAccessor { *; }

# ── Ktor (OkHttp engine on Android) ──────────────────────────────────────────
# Ktor ships consumer rules; keep OkHttp interop classes just in case.
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn io.ktor.utils.io.**

# ── Google Play Services Identity / Auth API ─────────────────────────────────
# play-services-auth has consumer rules but Identity is recent and sometimes
# missing entries — keep its public surface to be safe.
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.common.api.** { *; }

# ── Lucide icons (com.composables:icons-lucide-cmp) ──────────────────────────
# Image vectors are accessed via Kotlin extension properties; defaults handle
# this but be explicit since the package uses backing _Icon fields.
-keepclassmembers class com.composables.icons.lucide.** {
    *** _*;
}

# ── kotlinx-datetime / kotlin.time ───────────────────────────────────────────
-dontwarn kotlinx.datetime.**

# ── Crash reporting / debugging ──────────────────────────────────────────────
# Keep line numbers + source file for readable stack traces in Play Console.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
