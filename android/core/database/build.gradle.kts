plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // Note: KSP or KAPT is typically applied for Room compiler, 
    // but we write the bare library configuration for compiling.
}

android {
    namespace = "org.circle13.antara.core.database"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Room + SQLCipher configurations
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    // Sub-module dependency
    implementation(project(":core:model"))

    testImplementation("junit:junit:4.13.2")
}
