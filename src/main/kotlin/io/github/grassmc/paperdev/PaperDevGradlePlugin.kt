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
import io.github.grassmc.paperdev.dsl.PaperDevExtension
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.dsl.PaperVersions
import io.github.grassmc.paperdev.namespace.Namespace
import io.github.grassmc.paperdev.namespace.PluginNamespace
import io.github.grassmc.paperdev.namespace.PluginNamespaceFinder
import io.github.grassmc.paperdev.tasks.CollectPluginNamespacesTask
import io.github.grassmc.paperdev.tasks.PaperLibrariesJsonTask
import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
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
            registerTasks()
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


    private fun Project.registerTasks() {
        val collectPluginNamespaces = tasks.register<CollectPluginNamespacesTask>(COLLECT_PLUGIN_NAMESPACES_TASK_NAME) {
            group = TASK_GROUP
            description = "Collects the namespaces and it parents of all compiled classes."

            classes.from(compiledClasses())
            outputJsonFile = paperDevFile("$name/namespaces.json")
        }

        val detectPluginNamespaces = tasks.register(DETECT_PLUGIN_NAMESPACES_TASK_NAME) {
            group = TASK_GROUP
            description = "Detects plugin namespaces and set conventions for pluginYml namespaces."

            dependsOn(collectPluginNamespaces)
            doFirst {
                val jackson = JsonMapper().registerKotlinModule()
                val namespacesJson = collectPluginNamespaces.get().outputJsonFile.get().asFile
                val namespaces = jackson.readValue<List<Namespace>>(namespacesJson)

                this@registerTasks.extensions.getByType<PaperPluginYml>().apply {
                    main.convention(PluginNamespaceFinder.Type.MAIN.findFrom(namespaces)?.name?.let(::PluginNamespace))
                    bootstrapper.convention(PluginNamespaceFinder.Type.BOOTSTRAPPER.findFrom(namespaces)?.name?.let(::PluginNamespace))
                    loader.convention(PluginNamespaceFinder.Type.LOADER.findFrom(namespaces)?.name?.let(::PluginNamespace))
                }
            }
        }

        val pluginYaml = tasks.register<PaperPluginYmlTask>(PAPER_PLUGIN_YML_TASK_NAME) {
            group = TASK_GROUP
            description = "Generates a paper-plugin.yml file for the project."

            pluginYml = provider { this@registerTasks.extensions.findByType<PaperPluginYml>() }
            outputDir = paperDevDir(name)

            dependsOn(detectPluginNamespaces)
        }

        val paperLibrariesJson = tasks.register<PaperLibrariesJsonTask>("paperLibrariesJson") {
            group = TASK_GROUP
            description = "Generates a json file contains repositories and dependencies in the project."

            librariesRootComponent = configurations
                .named(PAPER_LIBS_CONFIGURATION_NAME)
                .map { it.incoming.resolutionResult.root }
            paperLibrariesJson = paperDevFile("$name/paper-libraries.json")
        }

        val paperLibClassLoader = tasks.register<Copy>("paperLibsClassLoader") {
            group = TASK_GROUP
            description = "Copies the paper libraries loader to the build directory."

            val pluginJar = PaperDevGradlePlugin::class.java.protectionDomain.codeSource.location
            from(zipTree(pluginJar)) {
                include(PAPER_LIBS_LOADER_TEMPLATE_FILENAME)
                expand("package" to DEFAULT_CLASS_PACKAGE)
                into(DEFAULT_CLASS_PACKAGE.replace('.', '/'))
            }
            into(paperDevFile(name))
        }

        tasks.withType<Jar> {
            dependsOn(pluginYaml, paperLibrariesJson)
            from(pluginYaml.map { it.outputDir })
            from(paperLibrariesJson.map { it.paperLibrariesJson })
        }

        plugins.withType<JavaPlugin> {
            extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
                java.srcDirs(paperLibClassLoader.map { it.destinationDir })
            }
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

        const val PAPER_LIBS_CONFIGURATION_NAME = "paperLibs"

        private const val TASK_GROUP = "paper development"
        const val PAPER_PLUGIN_YML_TASK_NAME = "paperPluginYml"
        const val DETECT_PLUGIN_NAMESPACES_TASK_NAME = "detectPluginNamespaces"
        const val COLLECT_PLUGIN_NAMESPACES_TASK_NAME = "collectPluginNamespaces"

        const val PAPER_DEV_DIR = "paperDev"
        private const val PAPER_LIBS_LOADER_TEMPLATE_FILENAME = "PaperLibsLoader.java"

        const val DEFAULT_CLASS_PACKAGE = "io.github.grassmc.paperdev.loader"
    }
}
