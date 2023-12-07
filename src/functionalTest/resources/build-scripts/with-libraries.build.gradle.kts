plugins {
    id("io.github.grassmc.paper-dev")
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    paperLibs("com.google.guava:guava:32.1.3-jre")
    paperLibs(platform("com.fasterxml.jackson:jackson-bom:2.16.0"))
    paperLibs("com.fasterxml.jackson.core:jackson-core")
    paperLibs("io.github.grassmc:paper-dev-gradle-plugin:0.1.0")
}
