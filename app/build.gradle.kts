import java.io.File
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

val canonicalKeystoreBase64 = "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"

val rootKeystoreFile = rootProject.file("debug.keystore")
val appKeystoreFile = file("debug.keystore")
val base64KeystoreFile = rootProject.file("debug.keystore.base64")

fun ensureKeystoreExists(target: File) {
    if (!target.exists() || target.length() < 1000L) {
        try {
            val base64Data = if (base64KeystoreFile.exists() && base64KeystoreFile.length() > 1000L) {
                base64KeystoreFile.readText().replace("\r", "").replace("\n", "").replace(" ", "")
            } else {
                canonicalKeystoreBase64
            }
            val decodedBytes = Base64.getDecoder().decode(base64Data)
            target.parentFile?.mkdirs()
            target.writeBytes(decodedBytes)
        } catch (e: Exception) {
            logger.warn("Could not materialize keystore to ${target.path}: ${e.message}")
        }
    }
}

ensureKeystoreExists(rootKeystoreFile)
ensureKeystoreExists(appKeystoreFile)

// Also ensure user home default keystore is aligned if ~/.android exists
val userHomeKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
ensureKeystoreExists(userHomeKeystore)

android {
    namespace = "com.manidigit.yadin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.manidigit.yadin"
        minSdk = 26
        targetSdk = 34
        versionCode = 52
        versionName = "1.43.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = if (rootKeystoreFile.exists() && rootKeystoreFile.length() > 1000L) rootKeystoreFile else appKeystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }
        create("release") {
            storeFile = if (rootKeystoreFile.exists() && rootKeystoreFile.length() > 1000L) rootKeystoreFile else appKeystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
