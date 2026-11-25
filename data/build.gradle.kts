plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "es.technical_task_iberia_android.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.data)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
