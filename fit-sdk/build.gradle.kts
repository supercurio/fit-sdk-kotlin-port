plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

kotlin {
    jvm()
    jvmToolchain(8)

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.named<Jar>("jvmJar") {
    archiveFileName.set("fit-${project.version}.jar")

    exclude("**/examples/**")
    exclude("**/csv/examples/**")
    exclude("**/plugins/examples/**")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val activityFileValidationPlugin by tasks.registering(Jar::class) {
    // 1. Set the name of the output file
    archiveBaseName.set("ActivityFileValidationPlugin")

    val jvmTarget = kotlin.targets.getByName("jvm")
    from(jvmTarget.compilations.getByName("main").output.allOutputs)

    include("**/ActivityFileValidation**")
    exclude("**/examples/**")
    exclude("**/csv/**")
    exclude("**/util/**")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// To generate documentation in HTML
val dokkaHtmlJar by tasks.registering(Jar::class) {
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

// To generate documentation in Javadoc
val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}