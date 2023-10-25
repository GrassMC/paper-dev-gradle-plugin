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

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.namespace.Namespace
import io.github.grassmc.paperdev.namespace.PluginNamespaceParser
import io.github.grassmc.paperdev.tasks.CollectPluginNamespacesTask
import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

abstract class PaperDevGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(JavaPlugin::class)

        registerPluginYmlExtension()
        afterEvaluate {
            registerTasks()
        }
    }

    private fun Project.registerPluginYmlExtension() = PaperPluginYml(this)
        .also { extensions.add(PLUGIN_YML_EXTENSION, it) }
        .apply {
            name.convention(project.name)
            version.convention(project.version.toString())
            apiVersion.convention(PaperPluginYml.ApiVersion.Default)
            description.convention(project.description)

            val namespacesProvider = namespacesProvider()
            main.convention(namespacesProvider.flatMap {
                provider { PluginNamespaceParser.Type.MAIN.parse(it)?.name }
            })
            bootstrapper.convention(namespacesProvider.flatMap {
                provider { PluginNamespaceParser.Type.BOOTSTRAPPER.parse(it)?.name }
            })
            loader.convention(namespacesProvider.flatMap {
                provider { PluginNamespaceParser.Type.LOADER.parse(it)?.name }
            })
        }

    private fun Project.namespacesProvider() = provider {
        tasks
            .getByName<CollectPluginNamespacesTask>(COLLECT_PLUGIN_NAMESPACES_TASK_NAME)
            .outputJsonFile
            .get()
            .asFile
            .let { JsonMapper().registerKotlinModule().readValue<List<Namespace>>(it) }
    }

    private fun Project.registerTasks() {
        val collectPluginNamespaces = tasks.register<CollectPluginNamespacesTask>(COLLECT_PLUGIN_NAMESPACES_TASK_NAME) {
            group = TASK_GROUP
            description = "Collects the namespaces and it parents of all compiled classes."

            classes.from(compiledClasses())
            outputJsonFile = paperDevFile("$name/namespaces.json")
        }

        val pluginYaml = tasks.register<PaperPluginYmlTask>(PAPER_PLUGIN_YML_TASK_NAME) {
            group = TASK_GROUP
            description = "Generates a paper-plugin.yml file for the project."

            pluginYml = provider { this@registerTasks.extensions.findByType<PaperPluginYml>() }
            outputDir = paperDevDir(name)

            dependsOn(collectPluginNamespaces)
        }

        tasks.withType<Jar> {
            dependsOn(pluginYaml)
            from(pluginYaml.map { it.outputDir })
        }
    }

    private fun Project.compiledClasses() = extensions
        .getByType<SourceSetContainer>()
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        .output
        .classesDirs

    private fun Project.paperDevFile(path: String) = layout.buildDirectory.file("$PAPER_DEV_DIR/$path")

    private fun Project.paperDevDir(path: String) = layout.buildDirectory.dir("$PAPER_DEV_DIR/$path")

    companion object {
        private const val PLUGIN_YML_EXTENSION = "pluginYml"

        private const val TASK_GROUP = "paper development"
        const val PAPER_PLUGIN_YML_TASK_NAME = "paperPluginYml"
        const val COLLECT_PLUGIN_NAMESPACES_TASK_NAME = "collectPluginNamespaces"

        const val PAPER_DEV_DIR = "paperDev"
    }
}
