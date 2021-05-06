// Top-level(build file where you can add configuration options common to all sub-projects/modules.)
buildscript {
    repositories {
        maven("http://maven.iclass30.com/repository/android-public/") {
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do(not place your application dependencies here; they belong)
        // in(the individual module build.gradle files)
    }
}

allprojects {
    repositories {
        maven("http://maven.iclass30.com/repository/android-public/") {
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}