// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

fun implementation(string: String) {
    dependencies {
        implementation("dev.chrisbanes.haze:haze:2.0.1") // or latest version
        implementation("dev.chrisbanes.haze:haze-blur:2.0.1") // for blur effects
    }

}


