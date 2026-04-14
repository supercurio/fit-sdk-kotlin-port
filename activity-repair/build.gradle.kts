plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    jvmToolchain(8)

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":fit"))
        }
    }
}

tasks.named<Jar>("jvmJar") {
    archiveFileName.set("ActivityRepairTool.jar")
    manifest {
        attributes["Main-Class"] = "com.garmin.fit.repair.ActivityRepairTool"
    }

    from(configurations.named("jvmRuntimeClasspath").map { configuration ->
        configuration
            .filter { it.path.contains("fit") }
            .map { file ->
                if (file.isDirectory) file else zipTree(file)
            }
    })

    exclude("**/examples/**")
    exclude("**/csv/**")
    exclude("**/util/**")
    exclude("**/plugins/**")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}