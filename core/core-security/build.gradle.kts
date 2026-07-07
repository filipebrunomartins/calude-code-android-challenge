plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.movieflux.core.security"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:core-common"))
    implementation(libs.javax.inject)
    implementation(libs.hilt.android)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
