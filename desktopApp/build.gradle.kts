import io.github.kdroidfilter.nucleus.desktop.application.dsl.CompressionLevel
import io.github.kdroidfilter.nucleus.desktop.application.dsl.TargetFormat
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    kotlin("jvm")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(11)
}

val appVersion: String = (findProperty("appVersion") as String?)
    ?.removePrefix("v")
    ?.takeIf { it.isNotBlank() }
    ?: "1.0.0"

// Project Leyden AOT cache (JVM mode only). Enabled on demand via -PenableAotCache=true
// to avoid running a training launch during GraalVM native-image builds.
val aotCacheEnabled: Boolean = (findProperty("enableAotCache") as String?)?.toBoolean() ?: false

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.kotlinx.datetime)
    implementation(libs.metrox.viewmodelCompose)
    implementation(libs.jewel.intUiStandalone)
    implementation(libs.nucleus.darkmodeDetector)
    implementation(libs.nucleus.decoratedWindowCore)
    implementation(libs.nucleus.decoratedWindowJni)
    implementation(libs.nucleus.decoratedWindowJewel)
    implementation(libs.nucleus.scheduler)
    implementation(libs.nucleus.notificationCommon)
    implementation(libs.nucleus.notificationWindows)
    implementation(libs.nucleus.notificationMacos)
    implementation(libs.nucleus.notificationLinux)
    implementation(libs.nucleus.coreRuntime)
    implementation(libs.nucleus.graalvmRuntime)
    implementation(libs.nucleus.aotRuntime)
    implementation(libs.nucleus.menuMacos)
}

nucleus.application {
    mainClass = "dev.nucleus.scheduleit.MainKt"

    buildTypes {
        release {
            proguard {
                version = "7.8.1"
                isEnabled = true
                optimize = true
                configurationFiles.from(project.file("proguard-rules.pro"))
            }
        }
    }

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb, TargetFormat.AppX)
        cleanupNativeLibs = true
        compressionLevel = CompressionLevel.Maximum
        enableAotCache = aotCacheEnabled
        packageName = "ScheduleIt"
        packageVersion = appVersion
        description = "Schedule and manage your tasks across desktop and mobile."
        vendor = "Elie Gambache"
        copyright = "© 2026 Elie Gambache. Licensed under GPLv3."
        homepage = "https://github.com/kdroidFilter/ScheduleIt"
        licenseFile.set(rootProject.file("LICENSE"))
        modules("java.sql", "jdk.httpserver")

        macOS {
            iconFile.set(rootProject.file("art/icon.icns"))
            bundleID = "dev.nucleus.scheduleit"
            dockName = "ScheduleIt"
            appCategory = "public.app-category.productivity"
            minimumSystemVersion = "12.0"
        }
        windows {
            iconFile.set(rootProject.file("art/icon.ico"))
            // Stable UUID — DO NOT change: required for MSI/NSIS upgrade detection.
            upgradeUuid = "5AC22F8D-A59F-4590-9AED-590CF72774E7"
            menuGroup = "ScheduleIt"
            perUserInstall = true
            nsis {
                multiLanguageInstaller = true
                installerLanguages = listOf("en_US", "fr_FR", "he_IL")
            }
            appx {
                applicationId = "ScheduleIt"
                identityName = "KdroidFilter.ScheduleIt-ClassTimetable"
                displayName = "ScheduleIt - Class Timetable"
                publisherDisplayName = "KdroidFilter"
                // Publisher CN provided by the Microsoft Partner Center for this Store listing.
                publisher = "CN=D541E802-6D30-446A-864E-2E8ABD2DAA5E"
                languages = listOf("en-US", "fr-FR", "he-IL")
                backgroundColor = "#FFFFFF"
                showNameOnTiles = true
                // Microsoft Store rejects MSIX packages with MinVersion <= 10.0.17134.0 (Win10 1803).
                // 10.0.17763.0 = Windows 10 1809 (October 2018 Update) — minimum accepted by the Store.
                minVersion = "10.0.17763.0"
                maxVersionTested = "10.0.22621.0"
                storeLogo.set(project.file("packaging/appx/StoreLogo.png"))
                square44x44Logo.set(project.file("packaging/appx/Square44x44Logo.png"))
                square150x150Logo.set(project.file("packaging/appx/Square150x150Logo.png"))
                wide310x150Logo.set(project.file("packaging/appx/Wide310x150Logo.png"))
            }
        }
        linux {
            iconFile.set(rootProject.file("art/icon.png"))
            debMaintainer = "Elie Gambache <elyahou.hadass@gmail.com>"
            menuGroup = "Office"
            shortcut = true
        }
    }

    graalvm {
        isEnabled = true
        imageName = "scheduleit"
        javaLanguageVersion = 25
        jvmVendor = JvmVendorSpec.BELLSOFT
        // "compatibility" by default to produce binaries that run on older CPUs and to
        // dodge GraalVM AVX-512 codegen bugs (VMOVDQU64 ZMM) on hosted runners.
        march = (findProperty("nativeMarch") as String?) ?: "compatibility"
        buildArgs.addAll(
            "-H:+AddAllCharsets",
            "-Djava.awt.headless=false",
            "-Os",
            "-H:-IncludeMethodData",
        )
    }
}
