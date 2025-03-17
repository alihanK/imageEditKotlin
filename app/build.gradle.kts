plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
}

android {
    namespace = "com.package.picchhanger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.package.picchhanger"
        minSdk = 21
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

    packaging {
        resources {
            pickFirsts.add("META-INF/DEPENDENCIES")
            pickFirsts.add("mozilla/public-suffix-list.txt")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.appcompat)

    //dagger2
    implementation (libs.dagger)
    kapt(libs.daggerCompiler)

    // Activity Compose
    implementation(libs.androidx.activity.compose)

    // Compose Navigation
    implementation (libs.androidx.navigation.compose)

    // Material 3 (MyAppTheme için)
    implementation (libs.material3)

    //coil
    implementation(libs.coil)

    implementation ("com.google.dagger:dagger:2.54")
    kapt ("com.google.dagger:dagger-compiler:2.54")





    implementation ("com.github.yalantis:ucrop:2.2.8-native")  // native versiyonunu kullanın


    implementation(libs.ucrop)



}