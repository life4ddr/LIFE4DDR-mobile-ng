import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kermit)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            implementation(libs.koin.composeViewModelNavigation)
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

tasks.register("generateSecretsFile") {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    val sanbaiId: String?
    val sanbaiSecret: String?

    if (localPropertiesFile.exists()) {
        println("Reading local.properties")
        localPropertiesFile.inputStream().use { properties.load(it) }
        sanbaiId = properties.getProperty("sanbai.app.id")
        sanbaiSecret = properties.getProperty("sanbai.app.secret")
    } else {
        sanbaiId = System.getenv("SANBAI_APP_ID")
        sanbaiSecret = System.getenv("SANBAI_APP_SECRET")
    }
    if (sanbaiId == null) { println("SANBAI_ID is null") }
    if (sanbaiSecret == null) { println("SANBAI_SECRET is null") }

    val outputFile = file("src/commonMain/kotlin/com/perrigogames/life4ddr/nextgen/feature/sanbai/api/SanbaiSecrets.kt")
    outputFile.parentFile.mkdirs()
    outputFile.writeText(
        """
        package com.perrigogames.life4ddr.nextgen.feature.sanbai.api

        object SanbaiSecrets {
            const val SANBAI_APP_ID = "${sanbaiId ?: "NO_ID"}"
            const val SANBAI_APP_SECRET = "${sanbaiSecret ?: "NO_SECRET"}"
        }
        """.trimIndent()
    )
    println("Generated Kotlin source file at ${outputFile.path}")
}

tasks.named("preBuild") {
    dependsOn("generateSecretsFile")
}
