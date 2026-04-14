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
    archiveFileName.set("FitCSVTool.jar")
    manifest {
        attributes["Main-Class"] = "com.garmin.fit.csv.CSVTool"
    }

    from(configurations.named("jvmRuntimeClasspath").map { configuration ->
        configuration
            .filter { it.path.contains("fit-sdk") }
            .map { file ->
                if (file.isDirectory) file else zipTree(file)
            }
    })

    exclude("**/examples/**")
    exclude("**/csv/examples/**")
    exclude("**/plugins/examples/**")
    exclude("**/repair/**")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}