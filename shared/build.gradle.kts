import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.mokoResources)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.kermit)
            implementation(libs.koin.core)
            implementation(libs.ksoup.entities)
            implementation(libs.ktor.clientAuth)
            implementation(libs.ktor.clientCio)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.clientSerializationJson)
            implementation(libs.russhwolf.settings)
            implementation(libs.russhwolf.settingsCoroutines)
            implementation(libs.russhwolf.settingsDatastore)
            implementation(libs.russhwolf.settingsSerialization)
            implementation(libs.sqldelight.coroutines)
            api(libs.moko.mvvmCore)
            api(libs.moko.mvvmFlow)
            api(libs.moko.mvvmCompose)
            api(libs.moko.mvvmFlowCompose)
            api(libs.moko.resources.core)
            api(libs.moko.resources.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            api(libs.moko.resources.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.sqldelight.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm)
        }
    }
}

android {
    namespace = "com.perrigogames.life4ddr.nextgen.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases.create("Life4Db") {
        packageName.set("com.perrigogames.life4ddr.nextgen")
//        dialect("sqlite:3.24")
    }
}

multiplatformResources {
    resourcesPackage.set("com.perrigogames.life4ddr.nextgen")
}
