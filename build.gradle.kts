// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.google.secrets)
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.hilt) apply false
    //alias(libs.plugins.compose.compiler) apply false
}

val debugVersionNameSuffix by extra("debug")
val releaseVersionNameSuffix by extra("release")
