plugins {
    alias(libs.plugins.android.application)
}

// ── Build Configuration ───────────────────────────────────
private val appId = "com.softpower"
private val compileSdkVersion = 37
private val targetSdkVersion = 37
private val minimumSdkVersion = 26
private val appVersionCode = 1
private val appVersionName = "1.0"
private val proguardRulesFile = "proguard-rules.pro"

private val optimizedPackages = setOf(
    "androidx.**",
    "kotlin.**",
    "kotlinx.**"
)

// ── Android Configuration ─────────────────────────────────
android {
    namespace = appId

    compileSdk {
        version = release(compileSdkVersion)
    }

    defaultConfig {
        applicationId = appId
        minSdk = minimumSdkVersion
        targetSdk = targetSdkVersion
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ── Build Types ───────────────────────────────────────
    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = optimizedPackages
            }

            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                proguardRulesFile
            )
        }
    }

    // ── Compile Options ───────────────────────────────────
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ── Dependencies ──────────────────────────────────────────
dependencies {
    // ── AndroidX ─────────────────────────────────────────
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)

    // ── Testing ──────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}