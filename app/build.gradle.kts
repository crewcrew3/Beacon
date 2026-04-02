import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "ru.itis.beacon"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "ru.itis.beacon"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        rootProject.file("local.properties").inputStream().use { properties.load(it) }
        val mapkitApiKey = properties.getProperty("MAPKIT_API_KEY", "")
        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapkitApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    //core modules
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.utils)
    implementation(projects.navigation.impl)
    implementation(projects.data.impl)
    //feature modules
    implementation(projects.feature.map.api)
    implementation(projects.feature.map.impl)
    implementation(projects.feature.login.impl)
    implementation(projects.feature.signup.impl)
    implementation(projects.feature.profile.impl)

    //androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)

    //compose
    implementation(libs.bundles.compose.deps)
    implementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)

    //DI
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    //навигация
    implementation(libs.bundles.nav.deps)

    //карты
    implementation(libs.yandex.maps)
}