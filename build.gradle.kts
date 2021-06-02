// Top-level(build file where you can add configuration options common to all sub-projects/modules.)
buildscript {
    repositories {
        google()
        maven ("https://maven.aliyun.com/repository/public/")
        maven ("https://s3.amazonaws.com/fabric-artifacts/public")
        maven ("https://maven.aliyun.com/repository/gradle-plugin")
        maven ("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do(not place your application dependencies here; they belong)
        // in(the individual module build.gradle files)
    }
}

allprojects {
    repositories {
        google()
        maven ("https://maven.aliyun.com/repository/public/")
        maven ("https://jitpack.io")
        maven ("https://maven.google.com/")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}