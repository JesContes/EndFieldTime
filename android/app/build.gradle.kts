import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// 发布签名：从 android/keystore.properties 读取（该文件与 jks 都不入库）
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

// ============================================================================
// 版本规则（全项目唯一来源：发版只改下面这一行版本名）
//
//   版本号固定两段：<主版本>.<次版本>，例如 1.2
//   - 小版本迭代（加功能、修 bug）：次版本 +1，如 1.2 → 1.3
//   - 大版本（重构 / 不兼容变更）：主版本 +1 且次版本归零，如 1.9 → 2.0
//
//   versionCode 由版本名自动推导 = 主*100 + 次（1.2 → 102），
//   保证每次发版严格递增，不需要手工维护，也不会忘记递增导致装不上。
// ============================================================================
val appVersionName = "1.2"
val appVersionCode: Int = appVersionName.split(".").let { parts ->
    require(parts.size == 2 && parts.all { it.toIntOrNull() != null }) {
        "版本名必须是 <主>.<次> 两段数字，当前为：$appVersionName"
    }
    parts[0].toInt() * 100 + parts[1].toInt()
}

android {
    namespace = "com.endfield.time"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.endfield.time"
        minSdk = 26
        targetSdk = 36
        // 每次发版必须递增，否则无法覆盖升级
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

// 产物命名：release 输出 endFieldTime-<versionName>.apk（便于直接分发与识别）
androidComponents {
    onVariants { variant ->
        if (variant.buildType == "release") {
            variant.outputs.forEach { output ->
                output.outputFileName.set("endFieldTime-$appVersionName.apk")
            }
        }
    }
}