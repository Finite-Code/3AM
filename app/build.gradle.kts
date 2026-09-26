import java.util.Properties

val versioningFile = rootProject.file("versioning.prop")
val versioningProps = Properties().apply {
    if (versioningFile.exists()) {
        versioningFile.inputStream().use { load(it) }
    }
}
val currentBuildCounter = versioningProps.getProperty("build.counter")?.toIntOrNull() ?: 1

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.slate.music"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.slate.music"
        minSdk = 33
        targetSdk = 37
        versionCode = currentBuildCounter
        versionName = "1.0.0-$currentBuildCounter"
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
    buildFeatures {
        compose = true
    }
}

tasks.register("incrementBuildCounter"){
    doLast {
        val nextCounter = currentBuildCounter + 1
        versioningProps.setProperty("build.counter", nextCounter.toString())
        versioningFile.outputStream().use {
            versioningProps.store(it, "Automated Build Counter")
        }
    }
}

tasks.matching { it.name.startsWith("assemble") || it.name.startsWith("bundle") }.configureEach {
    finalizedBy("incrementBuildCounter")
}

dependencies {
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("dev.chrisbanes.haze:haze:2.0.0-beta02")
    implementation("dev.chrisbanes.haze:haze-blur:2.0.0-beta02")
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material3:material3:1.5.0-alpha02")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
}