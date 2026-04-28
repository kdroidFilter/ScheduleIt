plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "dev.nucleus.scheduleit.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.nucleus.scheduleit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = (project.findProperty("androidVersionCode") as? String)?.toInt() ?: 1
        versionName = (project.findProperty("appVersion") as? String) ?: "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Signing config picked up from environment variables (CI) or
    // ~/.gradle/gradle.properties (local). Falls back to debug signing if any
    // value is missing — releases without these vars are unsigned.
    val keystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH")
        .orElse(providers.gradleProperty("androidKeystorePath")).orNull
    val keystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD")
        .orElse(providers.gradleProperty("androidKeystorePassword")).orNull
    val keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS")
        .orElse(providers.gradleProperty("androidKeyAlias")).orNull
    val keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD")
        .orElse(providers.gradleProperty("androidKeyPassword")).orNull

    val hasReleaseSigning = !keystorePath.isNullOrBlank() &&
        !keystorePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() &&
        !keyPassword.isNullOrBlank()

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        getByName("debug") {
            isMinifyEnabled = false
            // Suffix lets debug installs coexist with the release build on
            // the same device — avoids INSTALL_FAILED_UPDATE_INCOMPATIBLE.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.metrox.viewmodel)
    implementation(libs.filekit.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.icons.lucide)
    debugImplementation(libs.compose.uiTooling)

    // In-app updater: GitHub release polling + Ackpine package install.
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.ackpine.core)
    implementation(libs.ackpine.ktx)
}
