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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.assertEquals


@OptIn(ExperimentalPathApi::class)
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class FunctionalTestBase {
    @TempDir
    protected lateinit var projectDir: Path
    protected lateinit var buildScript: Path
    protected lateinit var settingsScript: Path
    protected lateinit var gradleRunner: GradleRunner

    protected val buildDir: Path get() = projectDir.resolve("build")
    protected val javaSourceDir: Path get() = projectDir.resolve("src/main/java")
    protected val kotlinSourceDir: Path get() = projectDir.resolve("src/main/kotlin")

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

    protected fun useBuildScript(name: String) {
        val buildScript = javaClass.getResource("/build-scripts/$name.build.gradle.kts")!!.toURI().toPath()
        buildScript.copyTo(this.buildScript, overwrite = true)
    }

    protected fun useSource(name: String, isKotlinSrc: Boolean = false) {
        val src = javaClass.getResource("/sources/$name")!!.toURI().toPath()
        val dest = if (isKotlinSrc) kotlinSourceDir else javaSourceDir
        dest.createDirectories()
        src.copyToRecursively(dest, followLinks = false, overwrite = true)
    }

    protected fun runProject(vararg args: String, action: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunner.withArguments(*args, "--stacktrace").build().apply(action)

    protected fun BuildResult.assertTaskSuccess(name: String) {
        assertEquals(TaskOutcome.SUCCESS, task(":$name")?.outcome)
    }

    protected fun BuildResult.assertTaskUpToDate(name: String) {
        assertEquals(TaskOutcome.UP_TO_DATE, task(":$name")?.outcome)
    }
}
