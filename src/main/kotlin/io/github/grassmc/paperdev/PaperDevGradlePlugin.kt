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
import io.github.grassmc.paperdev.namespace.EmptyNamespace
import io.github.grassmc.paperdev.namespace.PluginNamespaceFinder
import io.github.grassmc.paperdev.tasks.CollectBaseClassesTask
import io.github.grassmc.paperdev.tasks.PaperLibrariesJsonTask
import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
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
        val collectBaseClasses = tasks.register<CollectBaseClassesTask>(COLLECT_BASE_CLASSES_TASK_NAME) {
            description = "Collects base classes of the compiled classes from the project."

            classes.from(compiledClasses())
            skipNestedClass.convention(true)
            destinationDir = temporaryDirFactory.create()
        }

        val findEntryNamespaces = registerFindEntryNamespacesTasks(collectBaseClasses)
        val pluginYaml = tasks.register<PaperPluginYmlTask>(PAPER_PLUGIN_YML_TASK_NAME) {
            group = TASK_GROUP
            description = "Generates a paper-plugin.yml file for the project."

            pluginYml = provider { this@registerTasks.extensions.findByType<PaperPluginYml>() }
            outputDir = paperDevDir(name)

            dependsOn(findEntryNamespaces)
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

    private fun Project.registerFindEntryNamespacesTasks(collectBaseClasses: TaskProvider<CollectBaseClassesTask>) =
        tasks.register(FIND_ENTRY_NAMESPACES_TASK_NAME) {
            group = TASK_GROUP
            description = "Finds the entry namespaces and assigns default values to the pluginYml extension."

            dependsOn(collectBaseClasses)
            doFirst {
                val namespaces = collectBaseClasses.get().destinationDir.asFileTree.files.associate {
                    it.name to it.readLines().toSet()
                }
                this.extensions.configure<PaperPluginYml> {
                    if (main.orNull.let { it is EmptyNamespace || it == null }) {
                        val namespace = PluginNamespaceFinder.EntryFor.Main.find(namespaces)
                        main.convention(namespace)
                        if (namespace != EmptyNamespace) {
                            logger.debug("Main namespace founded: {}", namespace)
                        }
                    }
                    if (loader.orNull.let { it is EmptyNamespace || it == null }) {
                        val namespace = PluginNamespaceFinder.EntryFor.Loader.find(namespaces)
                        loader.convention(namespace)
                        if (namespace != EmptyNamespace) {
                            logger.debug("Loader namespace founded: {}", namespace)
                        }
                    }
                    if (bootstrapper.orNull.let { it is EmptyNamespace || it == null }) {
                        val namespace = PluginNamespaceFinder.EntryFor.Bootstrapper.find(namespaces)
                        bootstrapper.convention(namespace)
                        if (namespace != EmptyNamespace) {
                            logger.debug("Bootstrapper namespace founded: {}", namespace)
                        }
                    }
                }
            }
        }

    private fun Project.paperDevFile(path: String) = layout.buildDirectory.file("$PAPER_DEV_DIR/$path")

    private fun Project.paperDevDir(path: String) = layout.buildDirectory.dir("$PAPER_DEV_DIR/$path")

    companion object {
        private const val PLUGIN_YML_EXTENSION = "pluginYml"

        const val PAPER_LIBS_CONFIGURATION_NAME = "paperLibs"

        private const val TASK_GROUP = "paper development"
        const val FIND_ENTRY_NAMESPACES_TASK_NAME = "findEntryNamespaces"
        const val PAPER_PLUGIN_YML_TASK_NAME = "paperPluginYml"

        const val COLLECT_BASE_CLASSES_TASK_NAME = "collectBaseClasses"

        const val PAPER_DEV_DIR = "paperDev"
        private const val PAPER_LIBS_LOADER_TEMPLATE_FILENAME = "PaperLibsLoader.java"

        const val DEFAULT_CLASS_PACKAGE = "io.github.grassmc.paperdev.loader"
    }
}
