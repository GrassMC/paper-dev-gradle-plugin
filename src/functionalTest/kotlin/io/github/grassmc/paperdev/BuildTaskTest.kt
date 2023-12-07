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
import kotlin.test.Test

class BuildTaskTest : FunctionalTestBase() {
    @Test
    fun `should build project without configuration`() {
        useBuildScript("without-configuration")
        useSource("simple-java")
        runBuildTask {
            assertTaskSuccess("build")
        }
    }

    @Test
    fun `should build project with configuration`() {
        useBuildScript("with-configuration")
        useSource("simple-java")
        runBuildTask {
            assertTaskSuccess("build")
        }
    }

    @Test
    fun `should build project with kotlin src`() {
        useBuildScript("with-kotlin")
        useSource("simple-kotlin")
        runBuildTask {
            assertTaskSuccess("build")
        }
    }

    @Test
    fun `should build project with kotlin and java src`() {
        useBuildScript("with-kotlin")
        useSource("simple-kotlin")
        useSource("simple-java")
        runBuildTask {
            assertTaskSuccess("build")
        }
    }

    @Test
    fun `should build project with libraries`() {
        useBuildScript("with-libraries")
        useSource("simple-java")
        runBuildTask {
            assertTaskSuccess("build")
        }
    }

    private fun runBuildTask(action: BuildResult.() -> Unit) = runProject("build", action = action)
}
