plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.adisan_app_android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.adisan_app_android"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // ./gradlew app:assembleDebug -PfirebaseEmulador=true conecta la app al
            // Firebase Local Emulator Suite (firebase emulators:start) en vez del proyecto real
            val usarEmulador = (project.findProperty("firebaseEmulador") ?: "false").toString().toBoolean()
            buildConfigField("boolean", "FIREBASE_EMULADOR", usarEmulador.toString())
        }
        release {
            buildConfigField("boolean", "FIREBASE_EMULADOR", "false")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Firebase: autenticación y base de datos Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Retrofit solo aporta los tipos Call/Callback/Response que usan las pantallas;
    // las llamadas las resuelve FirebaseApiService contra Firestore
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Conversión JSON <-> objetos Java
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}