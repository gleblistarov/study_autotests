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
    // Source: https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
    implementation("org.seleniumhq.selenium:selenium-java:4.48.0")
}

tasks.test {
    val tags = (project.findProperty("tags") as String?)?.split(",")?.map { it.trim() }
    val excluded = (project.findProperty("excludeTags") as String?)?.split(",")?.map { it.trim() }

    useJUnitPlatform {
        if (tags != null) includeTags(*tags.toTypedArray())
        if (excluded != null) excludeTags(*excluded.toTypedArray())
    }
    testLogging {
        showStandardStreams = true
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
