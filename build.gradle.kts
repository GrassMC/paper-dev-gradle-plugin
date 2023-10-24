@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.prop(propName: String) = providers.gradleProperty(propName)

plugins {
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
}

group = prop("group").get()
version = prop("version").get()

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.yaml)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.AZUL
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin)
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin)

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin {
    testSourceSets.add(sourceSets["functionalTest"])

    val paperDev by plugins.creating {
        id = prop("plugin.id").get()
        implementationClass = prop("plugin.implementationClass").get()
        displayName = prop("plugin.displayName").get()
        description = prop("plugin.description").get()
        tags = prop("plugin.tags").map { it.split(",") }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    named<Task>("check") {
        dependsOn(testing.suites.named("functionalTest"))
    }

    validatePlugins {
        enableStricterValidation = true
    }
}
