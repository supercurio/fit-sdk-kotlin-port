plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

kotlin {
    jvm()
    jvmToolchain(21)

    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.collections.immutable)
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

tasks.register<JavaExec>("runDecodeExample") {
    registerExampleCli(desc = "FIT Decode Example Application", className = "DecodeExample")
}

tasks.register<JavaExec>("runDecoderExample") {
    this.registerExampleCli(desc = "FIT Decoder Example Application", className = "DecoderExample")
}

tasks.register<JavaExec>("runEncodeActivityExample") {
    this.registerExampleCli(desc = "FIT Encode Activity Example", className = "EncodeActivity")
}

tasks.register<JavaExec>("runEncodeCourseExample") {
    this.registerExampleCli(desc = "FIT Encode Course Example", className = "EncodeCourse")
}

tasks.register<JavaExec>("runEncodeExample") {
    this.registerExampleCli(desc = "FIT Encode Example", className = "EncodeExample")
}

tasks.register<JavaExec>("runEncodeWorkoutExample") {
    this.registerExampleCli(desc = "FIT Encode Workout Example", className = "EncodeWorkout")
}

tasks.register<JavaExec>("runFitDecoderExample") {
    this.registerExampleCli(desc = "FIT Decoder Example", className = "EncodeWorkout")
}

private fun JavaExec.registerExampleCli(desc: String, className: String) {
    group = "application"
    description = "Runs $desc"
    mainClass.set("com.garmin.fit.examples.$className")
    classpath = sourceSets["jvmMain"].runtimeClasspath

    if (project.hasProperty("args")) {
        args = project.property("args").toString().split(" ")
    }
}

tasks.register<Delete>("cleanFitFiles") {
    group = "cleanup"
    description = "Deletes all .fit files in the project directory"

    delete(fileTree(projectDir) {
        include("**/*.fit")
    })
}