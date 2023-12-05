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

package io.github.grassmc.paperdev.tasks

import io.github.grassmc.paperdev.PaperDevGradlePlugin
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.utils.YamlSerializer
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register

/**
 * Generates a `paper-plugin.yml` file for the project.
 */
@CacheableTask
abstract class PaperPluginYmlTask : DefaultTask() {
    /**
     * The Paper plugin description.
     */
    @get:Nested
    abstract val pluginYml: Property<PaperPluginYml>

    /**
     * The output directory to write the generated `paper-plugin.yml` to.
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val pluginYmlFile = outputDir.file(PAPER_PLUGIN_YML_FILENAME).get().asFile
        YamlSerializer.serialize(pluginYml.get()).also {
            pluginYmlFile.writeText(it)
        }
    }

    companion object {
        internal const val DEFAULT_NAME = "paperPluginYml"
        private const val PAPER_PLUGIN_YML_FILENAME = "paper-plugin.yml"
    }
}

internal fun Project.registerPaperPluginYmlTask() =
    tasks.register<PaperPluginYmlTask>(PaperPluginYmlTask.DEFAULT_NAME) {
        group = PaperDevGradlePlugin.TASK_GROUP
        description = "Generates a paper-plugin.yml file for the project."

        pluginYml.convention(provider { extensions.findByType<PaperPluginYml>() })
        outputDir.convention(layout.buildDirectory.dir(DEFAULT_PAPER_PLUGIN_YML_DIR))
    }

internal const val DEFAULT_PAPER_PLUGIN_YML_DIR =
    "${PaperDevGradlePlugin.PAPER_DEV_DIR}/${PaperPluginYmlTask.DEFAULT_NAME}"
