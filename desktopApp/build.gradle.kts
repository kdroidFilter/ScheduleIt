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
    implementation(libs.nucleus.coreRuntime)
    implementation(libs.nucleus.graalvmRuntime)
    implementation(libs.nucleus.menuMacos)
}

nucleus.application {
    mainClass = "dev.nucleus.scheduleit.MainKt"

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb)
        packageName = "ScheduleIt"
        packageVersion = "1.0.0"
        modules("java.sql")

        macOS {
            iconFile.set(rootProject.file("art/icon.icns"))
            bundleID = "dev.nucleus.scheduleit"
        }
        windows {
            iconFile.set(rootProject.file("art/icon.ico"))
        }
        linux {
            iconFile.set(rootProject.file("art/icon.png"))
        }
    }

    graalvm {
        isEnabled = true
        imageName = "scheduleit"
        javaLanguageVersion = 25
        jvmVendor = JvmVendorSpec.BELLSOFT
        buildArgs.addAll(
            "-H:+AddAllCharsets",
            "-Djava.awt.headless=false",
            "-Os",
            "-H:-IncludeMethodData",
        )
    }
}
