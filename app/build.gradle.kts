// ── Plugins ────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.application)
}

// ── Build Constants ────────────────────────────────────
private  val APP_ID = "com.softpower"
private  val COMPILE_SDK_VERSION = 37
private  val TARGET_SDK_VERSION = 37
private  val MIN_SDK_VERSION = 26
private  val VERSION_CODE = 1
private  val VERSION_NAME = "1.0"
private  val PROGUARD_RULES_FILE = "proguard-rules.pro"
private val OPTIMIZED_PACKAGES = setOf("androidx.**", "kotlin.**", "kotlinx.**")

// ── Android Configuration ──────────────────────────────
android {
    namespace = APP_ID

    compileSdk {
        version = release(COMPILE_SDK_VERSION)
    }

    defaultConfig {
        applicationId = APP_ID
        minSdk = MIN_SDK_VERSION
        targetSdk = TARGET_SDK_VERSION
        versionCode = VERSION_CODE
        versionName = VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ── Build Types ────────────────────────────────────
    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = OPTIMIZED_PACKAGES
            }
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                PROGUARD_RULES_FILE
            )
        }
    }

    // ── Compile Options ────────────────────────────────
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ── Dependencies ───────────────────────────────────────
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}