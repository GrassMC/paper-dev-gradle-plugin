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

import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType


abstract class PaperDevGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(JavaPlugin::class)

        registerTasks()
    }

    private fun Project.registerTasks() {
        val pluginYaml = tasks.register<PaperPluginYmlTask>(PAPER_PLUGIN_YML_TASK_NAME) {
            group = TASK_GROUP
            description = "Generates a paper-plugin.yml file for the project."
        }

        tasks.withType<Jar> {
            dependsOn(pluginYaml)
            from(pluginYaml.map { it.outputDir })
        }
    }

    companion object {
        private const val TASK_GROUP = "paper development"
        const val PAPER_PLUGIN_YML_TASK_NAME = "paperPluginYml"
    }
}
