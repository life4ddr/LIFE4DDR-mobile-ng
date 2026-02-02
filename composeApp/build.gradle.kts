import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.androidNavigationSafeArgs)
    alias(libs.plugins.mokoResources)
}

private val VERSION_CODE = 14
private val VERSION_NAME = "0.7.2"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
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
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.exifinterface)
            implementation(libs.androidx.navigation.compose)
//            implementation(libs.androidx.navigation.safeargs)
            implementation(libs.androidx.splashscreen)
            implementation(libs.accompanist.permissions)
            implementation(libs.composeWebview)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.coil.clientAndroid)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.ktor3)
            implementation(libs.composeBackhandler)
            implementation(libs.composeNavigation)
            implementation(libs.composeSettings.ui)
            implementation(libs.composeWebview)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.composeViewModelNavigation)
            implementation(libs.kermit)
            implementation(libs.russhwolf.settingsCoroutines)
            implementation(libs.russhwolf.settingsDatastore)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        appleMain.dependencies {
            implementation(libs.coil.clientDarwin)
            implementation(libs.koin.core)
            implementation(projects.shared)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.coil.clientJvm)
        }
    }
}

android {
    namespace = "com.perrigogames.life4ddr.nextgen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.perrigogames.life4ddr.nextgen"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = VERSION_CODE
        versionName = VERSION_NAME
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            addManifestPlaceholders(mapOf(Pair("providerName", "com.perrigogames.life4ddr.nextgen.fileprovider.debug")))
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            addManifestPlaceholders(mapOf(Pair("providerName", "com.perrigogames.life4ddr.nextgen.fileprovider")))
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.perrigogames.life4ddr.nextgen.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.perrigogames.life4ddr.nextgen"
            packageVersion = "1.0.0" // NOSHIP change to shared value after release
        }
    }
}

afterEvaluate {
    tasks.withType<JavaExec> {
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
