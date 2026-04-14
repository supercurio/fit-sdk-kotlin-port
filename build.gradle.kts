plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.dokka.javadoc) apply false
}

allprojects {
    group = "com.garmin"
    version = "21.200.0"
}