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

import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

abstract class PaperDevGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(JavaPlugin::class)

        registerPluginYmlExtension()

        val generatedResource = layout.buildDirectory.dir(GENERATED_RESOURCES_DIR)
        registerTasks(generatedResource)
    }

    private fun Project.registerPluginYmlExtension() = PaperPluginYml(this)
        .also { extensions.add(PLUGIN_YML_EXTENSION, it) }
        .apply {
            name.convention(project.name)
            version.convention(project.version.toString())
            apiVersion.convention(PaperPluginYml.ApiVersion.Default)
            description.convention(project.description)
        }

    private fun Project.registerTasks(generatedResource: Provider<Directory>) {
        val pluginYaml = tasks.register<PaperPluginYmlTask>(PAPER_PLUGIN_YML_TASK_NAME) {
            group = TASK_GROUP
            description = "Generates a paper-plugin.yml file for the project."

            pluginYml = provider { extensions.findByType<PaperPluginYml>() }
            outputDir = generatedResource
        }

        tasks.withType<Jar> {
            dependsOn(pluginYaml)
            from(pluginYaml.map { it.outputDir })
        }
    }

    companion object {
        private const val PLUGIN_YML_EXTENSION = "pluginYml"

        private const val TASK_GROUP = "paper development"
        const val PAPER_PLUGIN_YML_TASK_NAME = "paperPluginYml"

        const val PAPER_DEV_DIR = "paperDev"
        const val GENERATED_RESOURCES_DIR = "$PAPER_DEV_DIR/resources"
    }
}
