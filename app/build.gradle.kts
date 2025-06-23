plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.app2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app2025"
        minSdk = 31
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")  // Thêm classpath này
    }
}


dependencies {
    // --- AndroidX Core ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // --- Jetpack Compose (managed via BOM) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.runtime:runtime-livedata")

    // --- Firebase (BoM manages versions) ---
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth:23.0.0")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth")

    // Thêm dependency cho Firebase Realtime Database để lưu thông tin người dùng
    implementation("com.google.firebase:firebase-database")

    // Thêm các dependency cho Jetpack Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // --- Coil (hiển thị ảnh trong Compose) ---
    implementation("io.coil-kt:coil-compose:2.5.0")
    dependencies {
        implementation("io.coil-kt:coil-compose:2.5.0") // CHỈ ở đây
    }
    // --- OkHttp (upload ảnh lên Cloudinary hoặc server) ---
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // --- Accompanist (nếu cần paging, animation) ---
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // --- JSON ---
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.activity)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.play.services.maps)
    implementation(fileTree(mapOf(
        "dir" to "D:\\image_dacs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(fileTree(mapOf(
        "dir" to "D:\\image_dacs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation(fileTree(mapOf(
        "dir" to "D:\\image_dacs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation(libs.androidx.constraintlayout.core)

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // --- Debug-only ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")


}
