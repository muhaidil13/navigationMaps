plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
//    id("com.google.dagger.hilt.android")
//    id("kotlin-kapt")
}

android {
    namespace = "com.example.tugas_maps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tugas_maps"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.mapbox.navigationcore:android:3.5.0-rc.1") // Core SDK
    implementation("com.mapbox.navigationcore:ui-components:3.5.0-rc.1") // UI SDK
    implementation("com.google.android.gms:play-services-location:21.0.1")  // Google Location Service
    implementation(platform("com.google.firebase:firebase-bom:33.5.1")) // Firebase SDK
    implementation("com.google.firebase:firebase-auth") //Firebase Auth
    implementation("com.google.firebase:firebase-firestore")    // Firebase Firestore
    implementation("androidx.startup:startup-runtime:1.1.0")

//    // Hilt Android
//    implementation(libs.androidx.hilt.navigation.compose)
//    implementation(libs.hilt.android)
//    kapt(libs.hilt.compiler)



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat.resources)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.runtime.livedata)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}