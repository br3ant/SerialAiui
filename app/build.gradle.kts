plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId("com.br3ant.serialaiui")
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode(5)
        versionName("2.0")

        ndk {
            (this as com.android.build.gradle.internal.dsl.NdkOptions).abiFilter("armeabi-v7a")
        }

    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(setOf(File("libs")))
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.blankj:utilcodex:1.29.0")

    implementation("com.github.br3ant:tools:1.0.9")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation("com.tencent.bugly:crashreport:3.3.9")
}