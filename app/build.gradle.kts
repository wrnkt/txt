import java.util.Properties
import java.time.Instant

version = "0.0.1"

plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly(libs.junit.platform.launcher)

    implementation(libs.guava)
    implementation(libs.commons.lang3)
    implementation(libs.jackson.databind)

    implementation(libs.spring.boot.starter.log4j2)

    implementation(libs.jraw)
    implementation(libs.jsoup)
    implementation(libs.webmagic.core)
    implementation(libs.webmagic.extension)
    implementation(libs.java.semver)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val metadataResourceDir = layout.buildDirectory.dir("generated/resources/meta")

tasks.register("generateBuildMetadata") {
    val outputDir = metadataResourceDir.get().asFile
    outputs.dir(outputDir)

    doLast {
        outputDir.mkdirs()
        val propsFile = outputDir.resolve("build.properties")

        val gitCommit = "git rev-parse --short HEAD".runCommand()?.trim() ?: "unknown"
        val gitBranch = "git rev-parse --abbrev-ref HEAD".runCommand()?.trim() ?: "unknown"
        val buildTime = Instant.now().toString()

        propsFile.writeText(
            """
            version=${project.version}
            buildTime=$buildTime
            gitCommit=$gitCommit
            gitBranch=$gitBranch
            """.trimIndent()
        )
    }
}

tasks.processResources {
    dependsOn("generateBuildMetadata")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(metadataResourceDir) {
        into("")
    }
}

tasks.processTestResources {
    dependsOn("generateBuildMetadata")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(metadataResourceDir) {
        into("")
    }
}

sourceSets {
    main {
        resources.srcDir(metadataResourceDir)
    }
    test {
        resources.srcDir(metadataResourceDir)
    }
}

tasks.test {
    dependsOn("generateBuildMetadata")
}

application {
    mainClass = "org.tanchee.txt.App"
}

tasks.register("checkTypos") {
    group = "verification"
    description = "Run linters and quality checks"

    doLast {
        exec {
            commandLine("typos")
        }
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.test {
    testLogging {
        outputs.upToDateWhen { false }
        showStandardStreams = true
    }
}

// Helper to run git commands
fun String.runCommand(): String? = try {
    ProcessBuilder(*split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
} catch (e: Exception) {
    null
}
