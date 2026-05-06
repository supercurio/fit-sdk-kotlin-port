plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        jvmMain.dependencies {
            implementation(project(":fit"))
        }
    }
}

tasks.named<Jar>("jvmJar") {
    archiveFileName.set("ActivityRepairTool.jar")
    manifest {
        attributes["Main-Class"] = "com.garmin.fit.repair.ActivityRepairToolKt"
    }

    from(configurations.named("jvmRuntimeClasspath").map { configuration ->
        configuration.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })

    exclude("**/examples/**")
    exclude("**/csv/**")
    exclude("**/util/**")
    exclude("**/plugins/**")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}