@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    signing
    alias(libs.plugins.com.gradle.plugin.publish)
    alias(libs.plugins.org.sonarqube)
}

group = prop("group").get()
version = prop("version").get()

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.asm)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.AZUL
    }

    afterEvaluate {
        associateAssociateFunctionalTestCompilationWithMain()
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin)
        }

        val functionalTest by registering(JvmTestSuite::class) {
            testType = TestSuiteType.FUNCTIONAL_TEST
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
    website = prop("website")
    vcsUrl = prop("vcsUrl")
    testSourceSets.add(sourceSets["functionalTest"])

    val paperDev by plugins.creating {
        id = prop("plugin.id").get()
        implementationClass = prop("plugin.implementationClass").get()
        displayName = prop("plugin.displayName").get()
        description = prop("plugin.description").get()
        tags = prop("plugin.tags").map { it.split(",") }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey?.replace("\\n", "\n"), signingPassword)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    check {
        dependsOn(testing.suites.named("functionalTest"))
    }

    validatePlugins {
        enableStricterValidation = true
    }
}

fun Project.prop(propName: String) = providers.gradleProperty(propName)

fun KotlinJvmProjectExtension.associateAssociateFunctionalTestCompilationWithMain() {
    target.compilations {
        named("functionalTest") {
            associateWith(getByName(KotlinCompilation.MAIN_COMPILATION_NAME))
        }
    }
}
