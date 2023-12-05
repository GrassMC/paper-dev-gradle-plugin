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

import io.github.grassmc.paperdev.tasks.DEFAULT_GENERATED_DIR
import io.github.grassmc.paperdev.tasks.GeneratePluginLoaderTask
import io.github.grassmc.paperdev.tasks.PAPER_LIBS_LOADER_JAVA_PATH
import io.github.grassmc.paperdev.tasks.PAPER_LIBS_LOADER_JAVA_TEMPLATE
import org.gradle.testkit.runner.TaskOutcome
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratePluginLoaderTaskTest : FunctionalTestBase() {
    @Test
    fun `should generate PaperLibsLoader java src`() {
        usePlugin()
        val result = runGeneratePluginLoaderTask()
        val paperLibsLoaderFile = buildDir.resolve("$DEFAULT_GENERATED_DIR/$PAPER_LIBS_LOADER_JAVA_PATH")
        assertEquals(TaskOutcome.SUCCESS, result.task(":${GeneratePluginLoaderTask.DEFAULT_NAME}")!!.outcome)
        assertTrue(paperLibsLoaderFile.exists())
        assertEquals(PAPER_LIBS_LOADER_JAVA_TEMPLATE, paperLibsLoaderFile.readText())
    }

    @Test
    fun `should up-to-date when generated exist`() {
        usePlugin()
        val firstRun = runGeneratePluginLoaderTask()
        val secondRun = runGeneratePluginLoaderTask()
        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":${GeneratePluginLoaderTask.DEFAULT_NAME}")!!.outcome)
        assertEquals(TaskOutcome.UP_TO_DATE, secondRun.task(":${GeneratePluginLoaderTask.DEFAULT_NAME}")!!.outcome)
    }

    private fun runGeneratePluginLoaderTask() = runProject(GeneratePluginLoaderTask.DEFAULT_NAME)
}
