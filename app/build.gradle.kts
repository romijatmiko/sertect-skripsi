plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.skripsi.jalu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skripsi.jalu"
        minSdk = 26
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("com.itextpdf:itext7-core:7.1.16")
    implementation ("androidx.navigation:navigation-fragment-ktx")
    implementation ("androidx.navigation:navigation-ui-ktx")
    implementation ("com.airbnb.android:lottie:5.2.0")
    // Firebase libraries
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    // Coroutine
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // AndroidX libraries
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.paging:paging-runtime:3.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Image libraries
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // Networking libraries
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Other libraries
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha04")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.1.0")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")
}
