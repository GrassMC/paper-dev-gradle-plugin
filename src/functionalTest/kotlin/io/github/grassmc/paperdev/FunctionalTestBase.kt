/*
 * Copyright 2023 GrassMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.github.grassmc.paperdev

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.*


@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class FunctionalTestBase {
    @TempDir
    protected lateinit var projectDir: Path
    protected lateinit var buildScript: Path
    protected lateinit var settingsScript: Path
    protected lateinit var gradleRunner: GradleRunner

    protected val buildDir: Path get() = projectDir.resolve("build")
    protected val javaSourceDir: Path get() = projectDir.resolve("src/main/java")

    @BeforeEach
    fun setUp() {
        buildScript = projectDir.resolve("build.gradle.kts")
        buildScript.createFile()
        settingsScript = projectDir.resolve("settings.gradle.kts")
        settingsScript.createFile()
        gradleRunner = GradleRunner
            .create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
    }

    protected fun rootProjectName(name: String) {
        settingsScript.appendText("""rootProject.name = "$name"""")
    }

    protected fun appendBuildScript(script: String) {
        buildScript.appendText(script)
    }

    protected fun usePlugin(otherPlugins: String = "") {
        appendBuildScript(
            """
            plugins {
                id("io.github.grassmc.paper-dev")
                $otherPlugins
            }
            """.trimIndent()
        )
    }

    @OptIn(ExperimentalPathApi::class)
    protected fun useResourceProject(projectDir: String) {
        requireNotNull(javaClass.getResource("/$projectDir/"))
            .toURI()
            .toPath()
            .copyToRecursively(this.projectDir, followLinks = false, overwrite = true)
    }

    protected fun runProject(vararg args: String): BuildResult =
        gradleRunner.withArguments(*args, "--stacktrace").build()
}
