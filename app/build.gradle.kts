plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.gardening"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gardening"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    //5 new dependencies
    implementation ("androidx.camera:camera-core:1.4.1")
    implementation ("androidx.camera:camera-camera2:1.4.1")
    implementation ("androidx.camera:camera-lifecycle:1.4.1")
    implementation ("androidx.camera:camera-view:1.4.1")
    implementation ("com.google.guava:guava:30.1-android")

    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.android.volley:volley:1.2.0")

    // add the dependency for the Google AI client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Required for one-shot operations (to use `ListenableFuture` from Reactive Streams)
    implementation("com.google.guava:guava:31.0.1-android")

    // Required for streaming operations (to use `Publisher` from Guava Android)
    implementation("org.reactivestreams:reactive-streams:1.0.4")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("script-runtime"))
}