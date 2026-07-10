plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.healingjourney"
    compileSdk = 37

    // Enable BuildConfig
    buildFeatures {
        buildConfig = true
    }

    // Read the API key from local.properties
    val geminiApiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""

    defaultConfig {
        applicationId = "com.example.healingjourney"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiApiKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")  // ← ADD THIS for profile images

    // Image Loading - Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")  // ← ADD THIS
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")  // ← ADD THIS

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}