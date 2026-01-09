import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.mokoResources)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mockmp)
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
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest)
            implementation(libs.koin.test)
            implementation(libs.turbine)
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

mockmp {
    onTest {
        withHelper()
    }
}

@CacheableTask
abstract class GenerateSecretsFileTask : DefaultTask() {

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val localPropertiesFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val sanbaiAppId: Property<String>

    @get:Input
    @get:Optional
    abstract val sanbaiAppSecret: Property<String>

    @get:OutputFile
    abstract val secretsFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val sanbaiId = sanbaiAppId.getOrElse("NO_ID")
        val sanbaiSecret = sanbaiAppSecret.getOrElse("NO_SECRET")

        secretsFile.get().asFile.writeText(
            """
        package com.perrigogames.life4ddr.nextgen.feature.sanbai.api

        object SanbaiSecrets {
            const val SANBAI_APP_ID = "$sanbaiId"
            const val SANBAI_APP_SECRET = "$sanbaiSecret"
        }
        """.trimIndent()
        )
        println("Generated Kotlin source file at ${secretsFile.get().asFile.path}")
    }
}

tasks.register<GenerateSecretsFileTask>("generateSecretsFile") {
    val localProps = rootProject.file("local.properties")

    if (localProps.exists()) {
        localPropertiesFile.set(localProps)
        println("Using local.properties")
        val properties = Properties()
        localProps.inputStream().use { properties.load(it) }
        sanbaiAppId.set(properties.getProperty("sanbai.app.id"))
        sanbaiAppSecret.set(properties.getProperty("sanbai.app.secret"))
    } else {
        println("Using env variables")
        sanbaiAppId.set(providers.environmentVariable("SANBAI_APP_ID").orNull)
        sanbaiAppSecret.set(providers.environmentVariable("SANBAI_APP_SECRET").orNull)
    }

    secretsFile.set(layout.projectDirectory.file("src/commonMain/kotlin/com/perrigogames/life4ddr/nextgen/feature/sanbai/api/SanbaiSecrets.kt"))
}

tasks.named("preBuild") {
    dependsOn("generateSecretsFile")
}

tasks.named("compileKotlinJvm") {
    dependsOn("generateSecretsFile")
}
