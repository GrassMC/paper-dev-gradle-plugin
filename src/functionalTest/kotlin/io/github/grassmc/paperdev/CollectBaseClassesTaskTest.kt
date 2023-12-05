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

import io.github.grassmc.paperdev.tasks.CollectBaseClassesTask
import io.github.grassmc.paperdev.tasks.PAPER_LIBS_LOADER_JAVA_PATH
import org.gradle.testkit.runner.BuildResult
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectBaseClassesTaskTest : FunctionalTestBase() {
    @Test
    fun `should collect bases to destination dir`() {
        useResourceProject("sample-paper-plugin")
        runCollectBaseClassesTask {
            assertTaskSuccess(CollectBaseClassesTask.DEFAULT_NAME)
        }
        val destinationDir = buildDir.resolve("tmp/${CollectBaseClassesTask.DEFAULT_NAME}")
        assertTrue(destinationDir.exists())
        val generatedClassLoaderClass = destinationDir.resolve(
            PAPER_LIBS_LOADER_JAVA_PATH.removeSuffix(".java").replace('/', '.')
        )
        assertTrue(generatedClassLoaderClass.exists())
        val mainClass = destinationDir.resolve("io.github.grassmc.paperdev.sample.SamplePaperPlugin")
        assertTrue(mainClass.exists())
        assertEquals("org/bukkit/plugin/java/JavaPlugin", mainClass.readText().trimEnd())
        val otherClass = destinationDir.resolve("io.github.grassmc.paperdev.sample.TestClass1")
        assertTrue(otherClass.exists())
        assertEquals("java/lang/Runnable", otherClass.readText().trimEnd())
        val nestedClass = destinationDir.resolve("io.github.grassmc.paperdev.sample.SamplePaperPlugin\$Nested")
        assertFalse(nestedClass.exists())
        val emptyBaseClass = destinationDir.resolve("io.github.grassmc.paperdev.sample.WithEmptyBase")
        assertFalse(emptyBaseClass.exists())
    }

    @Test
    fun `should up-to-date when generated exist`() {
        useResourceProject("sample-paper-plugin")
        runCollectBaseClassesTask {
            assertTaskSuccess(CollectBaseClassesTask.DEFAULT_NAME)
        }
        runCollectBaseClassesTask {
            assertTaskUpToDate(CollectBaseClassesTask.DEFAULT_NAME)
        }
    }

    @Test
    fun `should include nested base classes when skipNestedClass is false`() {
        useResourceProject("sample-paper-plugin")
        appendBuildScript(
            """
            afterEvaluate {
                tasks.named<${CollectBaseClassesTask::class.qualifiedName}>("${CollectBaseClassesTask.DEFAULT_NAME}") {
                    skipNestedClass.set(false)
                }
            }
            """
        )
        runCollectBaseClassesTask {
            assertTaskSuccess(CollectBaseClassesTask.DEFAULT_NAME)
        }
        val destinationDir = buildDir.resolve("tmp/${CollectBaseClassesTask.DEFAULT_NAME}")
        assertTrue(destinationDir.exists())
        val nestedClass = destinationDir.resolve("io.github.grassmc.paperdev.sample.SamplePaperPlugin\$Nested")
        assertTrue(nestedClass.exists())
        assertEquals("java/lang/Runnable", nestedClass.readText().trimEnd())
    }

    private fun runCollectBaseClassesTask(action: BuildResult.() -> Unit) =
        runProject(CollectBaseClassesTask.DEFAULT_NAME, action = action)
}
