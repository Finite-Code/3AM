import java.util.Properties

val versioningFile = rootProject.file("versioning.prop")
val versioningProps = Properties().apply {
    if (versioningFile.exists()) {
        versioningFile.inputStream().use { load(it) }
    }
}
val currentBuildCounter = versioningProps.getProperty("build.counter")?.toIntOrNull() ?: 1
val verMajor = versioningProps.getProperty("version.major") ?: "1"
val verMinor = versioningProps.getProperty("version.minor") ?: "1"
val verPatch = versioningProps.getProperty("version.patch") ?: "0"
val verStatus = versioningProps.getProperty("version.status") ?: "stable"

val formattedVersionName = "${verMajor}.${verMinor}.${verPatch}-${verStatus}"

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
        versionName = formattedVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations.addAll(listOf("en"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // optimize debug builds too
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
    description = "Updates release notes and increments build count on every succesful build."
    doLast {
        // REGEX MATCHING WAS GENERATED USING LLMs
        val relNotesFile = rootProject.file("releasenotes.latest")
        if (relNotesFile.exists()) {
            val notesText = relNotesFile.readText()
            val updatedNotes = if (notesText.contains(Regex("""(?m)^v\d+\.\d+\.\d+"""))) {
                notesText.replaceFirst(Regex("""(?m)^v\d+\.\d+\.\d+.*"""), "v$formattedVersionName")
            } else {
                "# 3AM\nv$formattedVersionName\n\n" + notesText
            }
            relNotesFile.writeText(updatedNotes)
        }

        val nextCounter = currentBuildCounter + 1
        versioningFile.writeText(
            """
            version.major=$verMajor
            version.minor=$verMinor
            version.patch=$verPatch
            version.status=$verStatus
            build.counter=$nextCounter
            """.trimIndent() + "\n"
        )
    }
}

tasks.matching { it.name.startsWith("assemble") || it.name.startsWith("bundle") }.configureEach {
    finalizedBy("incrementBuildCounter")
}

dependencies {
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("dev.chrisbanes.haze:haze:2.0.0")
    implementation("dev.chrisbanes.haze:haze-blur:2.0.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
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
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material3:material3:1.5.0-alpha29")
    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-session:1.11.1")
}