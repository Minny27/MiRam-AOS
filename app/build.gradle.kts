import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val keystoreProperties = Properties().apply {
    val file = rootProject.file("key.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun propertyOrEnv(name: String, envName: String = name): String? =
    providers.gradleProperty(name).orNull
        ?: System.getenv(envName)
        ?: keystoreProperties.getProperty(name)

val releaseStoreFile = propertyOrEnv("storeFile")
val releaseStorePassword = propertyOrEnv("storePassword")
val releaseKeyAlias = propertyOrEnv("keyAlias")
val releaseKeyPassword = propertyOrEnv("keyPassword")
val hasReleaseSigning =
    listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
        .all { !it.isNullOrBlank() }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.miram"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.seungmin.miram"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.gradleProperty("appVersionCode").orNull?.toIntOrNull() ?: 1
        versionName = providers.gradleProperty("appVersionName").orNull ?: "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // 소스 파일이 루트 하위 flat 디렉토리에 있으므로 상대 경로로 지정
    sourceSets {
        getByName("main") {
            java.setSrcDirs(
                listOf(
                    "src/main/kotlin",
                    "../features",
                    "../routes",
                    "../shared"
                )
            )
        }

    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

afterEvaluate {
    val debugPrerequisites = listOf(
        "mergeDebugAssets",
        "compressDebugAssets",
        "mergeDebugJniLibFolders",
        "validateSigningDebug",
        "mergeExtDexDebug",
        "mergeLibDexDebug",
        "checkDebugDuplicateClasses",
    )

    tasks.matching { it.name == "kspDebugKotlin" }.configureEach {
        dependsOn(debugPrerequisites)
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
        if (name == "compileDebugKotlin") {
            dependsOn(debugPrerequisites)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Material Components
    implementation(libs.material)
}
