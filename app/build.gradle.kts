import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val versionProps = Properties().apply {
    val propsFile = rootProject.file("version.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

val resolvedVersionName = (
    findProperty("versionName")?.toString()?.takeIf { it.isNotBlank() }
        ?: versionProps.getProperty("VERSION_NAME", "1.0.0")
).removePrefix("v").trim()

val buildNumber = (findProperty("buildNumber")?.toString()?.toIntOrNull() ?: 1).coerceIn(0, 9_999)

val resolvedVersionCode = run {
    val explicit = findProperty("versionCode")?.toString()?.toIntOrNull()
    if (explicit != null && explicit > 0) {
        explicit
    } else {
        val core = resolvedVersionName.substringBefore('-')
        val parts = core.split('.').mapNotNull { it.toIntOrNull() }
        val major = parts.getOrElse(0) { 1 }.coerceIn(0, 20)
        val minor = parts.getOrElse(1) { 0 }.coerceIn(0, 99)
        val patch = parts.getOrElse(2) { 0 }.coerceIn(0, 99)
        (major * 100_000_000) + (minor * 1_000_000) + (patch * 10_000) + buildNumber
    }
}

val keystoreFilePath = System.getenv("SIGNING_KEYSTORE_PATH")
val storePasswordEnv = System.getenv("SIGNING_STORE_PASSWORD")
val keyAliasEnv = System.getenv("SIGNING_KEY_ALIAS")
val keyPasswordEnv = System.getenv("SIGNING_KEY_PASSWORD")
val hasReleaseSigning = !keystoreFilePath.isNullOrBlank() &&
    !storePasswordEnv.isNullOrBlank() &&
    !keyAliasEnv.isNullOrBlank() &&
    !keyPasswordEnv.isNullOrBlank() &&
    file(keystoreFilePath).exists()

android {
    namespace = "com.darkxwn.payping"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.darkxwn.payping"
        minSdk = 26
        targetSdk = 35
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreFilePath!!)
                storePassword = storePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
