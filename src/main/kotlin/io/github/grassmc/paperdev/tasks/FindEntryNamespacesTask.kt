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
import io.github.grassmc.paperdev.namespace.EmptyNamespace
import io.github.grassmc.paperdev.namespace.PluginNamespace
import io.github.grassmc.paperdev.namespace.PluginNamespaceFinder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure

internal const val FIND_ENTRY_NAMESPACES_TASK_NAME = "findEntryNamespaces"

internal fun Project.registerFindEntryNamespacesTask(baseClassesDir: Provider<DirectoryProperty>) =
    tasks.register(FIND_ENTRY_NAMESPACES_TASK_NAME) {
        group = PaperDevGradlePlugin.TASK_GROUP
        description = "Finds the entry namespaces and assigns default values to the pluginYml extension."

        doFirst {
            val namespaces = baseClassesDir.get().asFileTree.files.associate {
                it.name to it.readLines().toSet()
            }
            this@registerFindEntryNamespacesTask.extensions.configure<PaperPluginYml> {
                PluginNamespaceFinder.EntryFor.Main.run {
                    findAndSetDefault(namespaces, main).also {
                        logIfFound(it, this)
                    }
                }
                PluginNamespaceFinder.EntryFor.Loader.run {
                    findAndSetDefault(namespaces, loader).also {
                        logIfFound(it, this)
                    }
                }
                PluginNamespaceFinder.EntryFor.Bootstrapper.run {
                    findAndSetDefault(namespaces, bootstrapper).also {
                        logIfFound(it, this)
                    }
                }
            }
        }
    }

private fun Task.logIfFound(namespace: PluginNamespace, entryFor: PluginNamespaceFinder.EntryFor) {
    if (namespace !is EmptyNamespace) {
        logger.debug("{} namespace founded: {}", entryFor.name, namespace)
    }
}

private fun PluginNamespaceFinder.EntryFor.findAndSetDefault(
    namespaces: Map<String, Set<String>>,
    property: Property<PluginNamespace>
): PluginNamespace {
    val currentNamespace = property.orNull
    if (currentNamespace != null && currentNamespace != EmptyNamespace) {
        return EmptyNamespace
    }

    return find(namespaces).also { property.convention(it) }
}
