// build.gradle (Project level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath ("com.google.gms:google-services:4.4.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")
    }
}

