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

import io.github.grassmc.paperdev.dsl.PaperDevExtension
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.dsl.PaperVersions
import io.github.grassmc.paperdev.tasks.*
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
        configurations.maybeCreate(PAPER_LIBS_CONFIGURATION_NAME).also {
            configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(it)
        }

        configurePaperRepository()
        registerPaperDevExtension()
        registerPluginYmlExtension()
        afterEvaluate {
            configureTasks()
        }
    }

    private fun Project.configurePaperRepository() = repositories {
        maven {
            name = "PaperMC Public Repository"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    private fun Project.registerPaperDevExtension() {
        val paperDev = extensions.create<PaperDevExtension>("paperDev").apply {
            version.convention(PaperVersions.Latest)
        }

        afterEvaluate {
            dependencies.add(
                JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                paperDev.version.map { it.toDependencyNotation() }
            )
        }
    }

    private fun Project.registerPluginYmlExtension() = PaperPluginYml(this)
        .also { extensions.add(PLUGIN_YML_EXTENSION, it) }
        .apply {
            name.convention(project.name)
            version.convention(provider { project.version.toString() })
            apiVersion.convention(PaperPluginYml.ApiVersion.Default)
            description.convention(project.description)
        }


    private fun Project.configureTasks() {
        val generatePluginLoader = registerGeneratePluginLoaderTask()
        extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
            java.srcDirs(generatePluginLoader.map { it.generatedDirectory })
        }

        val collectBaseClasses = registerCollectBaseClassesTask()
        val findEntryNamespaces = registerFindEntryNamespacesTask(collectBaseClasses.map { it.destinationDir }).apply {
            configure {
                dependsOn(collectBaseClasses)
            }
        }
        val pluginYaml = registerPaperPluginYmlTask().apply {
            configure {
                dependsOn(findEntryNamespaces)
            }
        }
        val paperLibrariesJson = registerPaperLibrariesJsonTask()
        tasks.withType<Jar> {
            dependsOn(pluginYaml, paperLibrariesJson)
            from(pluginYaml.map { it.outputDir })
            from(paperLibrariesJson.map { it.paperLibrariesJson })
        }
    }

    companion object {
        const val PAPER_LIBS_CONFIGURATION_NAME = "paperLibs"
        const val PAPER_DEV_DIR = "paperDev"

        internal const val TASK_GROUP = "paper development"
        private const val PLUGIN_YML_EXTENSION = "pluginYml"
    }
}
