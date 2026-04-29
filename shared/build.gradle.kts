import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidLibrary {
        namespace = "dev.nucleus.scheduleit.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources {
            enable = true
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.coroutinesCore)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serializationJson)
                implementation(libs.filekit.dialogsCompose)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutinesExtensions)
                implementation(libs.metrox.viewmodel)
                implementation(libs.metrox.viewmodelCompose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val nonJvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.compose.unstyled)
                implementation(libs.icons.lucide)
            }
        }

        val androidMain by getting {
            dependsOn(nonJvmMain)
            dependencies {
                implementation(libs.sqldelight.androidDriver)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.activity.compose)
                implementation(libs.play.services.auth)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.serializationJson)
            }
        }

        val iosMain by getting {
            dependsOn(nonJvmMain)
            dependencies {
                implementation(libs.sqldelight.nativeDriver)
            }
        }

        val jvmMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/source/driveOAuth/jvmMain"))
            dependencies {
                implementation(libs.sqldelight.sqliteDriver)
                implementation(libs.jewel.intUiStandalone)
                implementation(libs.intellij.icons)
                implementation(libs.nucleus.decoratedWindowCore)
                implementation(libs.nucleus.decoratedWindowJewel)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.nucleus.nativeHttpKtor)
            }
        }
    }
}

sqldelight {
    databases {
        create("ScheduleDatabase") {
            packageName.set("dev.nucleus.scheduleit.db")
            dialect(libs.sqldelight.sqlite324Dialect)
        }
    }
}

val generateDriveOAuthConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/driveOAuth/jvmMain")
    val clientId = providers.environmentVariable("GOOGLE_DRIVE_CLIENT_ID").orElse("")
    val clientSecret = providers.environmentVariable("GOOGLE_DRIVE_CLIENT_SECRET").orElse("")
    inputs.property("clientId", clientId)
    inputs.property("clientSecret", clientSecret)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get()
            .file("dev/nucleus/scheduleit/data/drive/DriveOAuthConfig.kt")
            .asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            // Generated file. Do not edit.
            package dev.nucleus.scheduleit.data.drive

            internal object DriveOAuthConfig {
                const val CLIENT_ID: String = "${clientId.get()}"
                const val CLIENT_SECRET: String = "${clientSecret.get()}"
                val isConfigured: Boolean get() = CLIENT_ID.isNotEmpty() && CLIENT_SECRET.isNotEmpty()
            }
            """.trimIndent(),
        )
    }
}

tasks.matching { it.name == "compileKotlinJvm" }.configureEach {
    dependsOn(generateDriveOAuthConfig)
}

// Hebrew uses the legacy ISO 639-1 code "iw" on Android < 4.2 and "he" from 4.2+.
// Mirror values-he into a temporary values-iw at build time and remove it once
// Compose has consumed it, so the source tree stays clean.
abstract class DuplicateHebrewStringsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceFile = project.file("src/commonMain/composeResources/values-he/strings.xml")

    @get:OutputFile
    val targetFile = project.file("src/commonMain/composeResources/values-iw/strings.xml")

    init {
        onlyIf { sourceFile.exists() }
    }

    @TaskAction
    fun duplicateStrings() {
        targetFile.parentFile.mkdirs()
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}

abstract class CleanupValuesIwDirectoryTask : DefaultTask() {
    private val valuesIwDir = project.file("src/commonMain/composeResources/values-iw")

    @TaskAction
    fun cleanupDirectory() {
        if (valuesIwDir.exists()) {
            valuesIwDir.deleteRecursively()
        }
    }
}

tasks.register<DuplicateHebrewStringsTask>("duplicateHebrewStrings") {
    description = "Duplicates Hebrew strings from values-he to values-iw for legacy Android compatibility"
    group = "build"
}

tasks.register<CleanupValuesIwDirectoryTask>("cleanupValuesIwDirectory") {
    description = "Deletes the temporary values-iw directory once Compose resources have been prepared"
    group = "build"
}

// All Compose tasks that read from commonMain must see values-iw and trigger cleanup afterwards.
val composeCommonMainResourceTasks = setOf(
    "prepareComposeResourcesTaskForCommonMain",
    "convertXmlValueResourcesForCommonMain",
    "copyNonXmlValueResourcesForCommonMain",
)

tasks.matching { it.name in composeCommonMainResourceTasks }.configureEach {
    dependsOn("duplicateHebrewStrings")
    finalizedBy("cleanupValuesIwDirectory")
}

compose.resources {
    publicResClass = true
}
