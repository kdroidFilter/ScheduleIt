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
                implementation(libs.compose.material3)
            }
        }

        val androidMain by getting {
            dependsOn(nonJvmMain)
            dependencies {
                implementation(libs.sqldelight.androidDriver)
            }
        }

        val iosMain by getting {
            dependsOn(nonJvmMain)
            dependencies {
                implementation(libs.sqldelight.nativeDriver)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqliteDriver)
                implementation(libs.jewel.intUiStandalone)
                implementation(libs.intellij.icons)
                implementation(libs.nucleus.decoratedWindowCore)
                implementation(libs.nucleus.decoratedWindowJewel)
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

compose.resources {
    publicResClass = true
}
