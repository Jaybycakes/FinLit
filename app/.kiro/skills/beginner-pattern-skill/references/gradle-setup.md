# Gradle Setup

## libs.versions.toml (extract only what this stack needs)

```toml
[versions]
agp = "9.2.1"
kotlin-core = "2.3.21"
kotlin-serialization = "1.11.0"
coroutines = "1.10.2"
ksp = "2.3.7"
jvm-target = "21"

# DI
koin = "4.1.1"
koin-annotation = "2.3.1"

# Network
ktorfit = "2.7.3"
ktor = "3.4.3"
okhttp = "5.3.2"
chucker = "4.3.1"

# UI
orbit-mvi = "11.0.0"
lifecycle = "2.10.0"
activity = "1.13.0"
compose-bom = "2026.04.01"
material3 = "1.4.0"
navigation3-ui = "1.1.1"
navigation3-lifecycle = "2.11.0-beta01"
coil = "3.4.0"

[libraries]
# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlin-serialization = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlin-serialization" }

# Koin BOM + annotations
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose" }
koin-compose-viewmodel = { group = "io.insert-koin", name = "koin-compose-viewmodel" }
koin-compose-viewmodel-navigation = { group = "io.insert-koin", name = "koin-compose-viewmodel-navigation" }
koin-annotation = { group = "io.insert-koin", name = "koin-annotations", version.ref = "koin-annotation" }
koin-ksp = { group = "io.insert-koin", name = "koin-ksp-compiler", version.ref = "koin-annotation" }

# Network
ktorfit = { group = "de.jensklingenberg.ktorfit", name = "ktorfit-lib", version.ref = "ktorfit" }
okhttp-bom = { group = "com.squareup.okhttp3", name = "okhttp-bom", version.ref = "okhttp" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp" }
okhttp-log = { group = "com.squareup.okhttp3", name = "logging-interceptor" }
ktor-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-serialization = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
ktor-auth = { group = "io.ktor", name = "ktor-client-auth", version.ref = "ktor" }
chucker = { group = "com.github.chuckerteam.chucker", name = "library", version.ref = "chucker" }
chucker-no-op = { group = "com.github.chuckerteam.chucker", name = "library-no-op", version.ref = "chucker" }

# Orbit MVI
orbit-mvi-core = { group = "org.orbit-mvi", name = "orbit-core", version.ref = "orbit-mvi" }
orbit-mvi-viewmodel = { group = "org.orbit-mvi", name = "orbit-viewmodel", version.ref = "orbit-mvi" }
orbit-mvi-compose = { group = "org.orbit-mvi", name = "orbit-compose", version.ref = "orbit-mvi" }

# Lifecycle
androidx-lifecycle-viewmodelCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtimeCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Compose
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "material3" }

# Navigation 3
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3-ui" }
navigation3-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "navigation3-lifecycle" }

# Image loading
coil = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coil" }

[bundles]
orbit-mvi = ["orbit-mvi-core", "orbit-mvi-viewmodel", "orbit-mvi-compose"]
koin-android = ["koin-android", "koin-androidx-compose", "koin-compose-viewmodel", "koin-compose-viewmodel-navigation"]
okhttp = ["okhttp", "okhttp-log"]
navigation3 = ["navigation3-ui", "navigation3-viewmodel"]
coroutines = ["coroutines-core", "coroutines-android"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin-core" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin-core" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin-core" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
ktorfit = { id = "de.jensklingenberg.ktorfit", version.ref = "ktorfit" }
```

## app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "com.example.myapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions { jvmTarget = "21" }
    buildFeatures { compose = true }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation 3
    implementation(libs.bundles.navigation3)

    // Orbit MVI
    implementation(libs.bundles.orbit.mvi)

    // Koin BOM + annotations
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
    implementation(libs.koin.annotation)
    ksp(libs.koin.ksp)

    // Ktorfit + Ktor + OkHttp
    implementation(libs.ktorfit)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.okhttp)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.auth)

    // Serialization + Coroutines
    implementation(libs.kotlin.serialization)
    implementation(libs.bundles.coroutines)

    // Image loading
    implementation(libs.coil)
    implementation(libs.coil.okhttp)

    // Debug network inspector (no-op in release)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
```

## Serialization: why it's needed

`kotlinx-serialization-json` is required for **two purposes**:

1. **Ktor ContentNegotiation** — `ktor-serialization-kotlinx-json` uses it to parse JSON responses into `@Serializable` data classes.
2. **Navigation 3 routes** — `@Serializable` data classes as type-safe routes require the serialization plugin + runtime.

Without the `kotlin-serialization` plugin in `plugins {}`, the `@Serializable` annotation won't generate serializers at compile time and both will fail at runtime.

## AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## proguard-rules.pro

```
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
# Ktorfit generated _Impl classes
-keep class **_Impl { *; }
-keep class **_Impl$* { *; }
```
