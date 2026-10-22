plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.seleniumhq.selenium:selenium-java:4.48.0")
    testImplementation("io.rest-assured:rest-assured:5.5.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

tasks.test {
    val tags = (project.findProperty("tags") as String?)?.split(",")?.map { it.trim() }
    val excluded = (project.findProperty("excludeTags") as String?)?.split(",")?.map { it.trim() }

    useJUnitPlatform {
        if (tags != null) includeTags(*tags.toTypedArray())
        if (excluded != null) excludeTags(*excluded.toTypedArray())
    }

    val runsSelectedTests = gradle.startParameter.taskRequests
        .flatMap { it.args }
        .any { it == "--tests" || it.startsWith("--tests=") }
    if (tags == null && !runsSelectedTests) {
        (options as JUnitPlatformOptions).excludeTags("api", "ui")
    }
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Test>("apiTest") {
    description = "Запускает только API-тесты (тег api)"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("api")
    }
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Test>("uiTest") {
    description = "Запускает только UI-тесты (тег ui)"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("ui")
    }
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register("finishTests") {
    dependsOn("test")
    doLast {
        println("Test run is over")
    }
}
