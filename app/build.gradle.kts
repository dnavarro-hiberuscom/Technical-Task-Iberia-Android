plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "es.technical_task_iberia_android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "es.technical_task_iberia_android"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    constraints {
        implementation(libs.androidx.concurrent.futures) {
            because("profileinstaller pulls 1.1.0 and androidx.test.ext:junit needs 1.2.0; AGP aligns both classpaths")
        }
    }

    implementation(project(":presentation"))
    implementation(project(":data"))

    implementation(platform(libs.koin.bom))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.app)

    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.bundles.app.test)

    // Test-only: the instrumented flow test swaps the repository for a fake with controlled data.
    androidTestImplementation(project(":domain"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
