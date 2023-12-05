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
                PluginNamespaceFinder.EntryFor.Main.findAndSetDefault(namespaces, main)
                    .takeUnless { it is EmptyNamespace }
                    ?.let { logger.debug("Main namespace founded: {}", it) }
                PluginNamespaceFinder.EntryFor.Loader.findAndSetDefault(namespaces, loader)
                    .takeUnless { it is EmptyNamespace }
                    ?.let { logger.debug("Loader namespace founded: {}", it) }
                PluginNamespaceFinder.EntryFor.Bootstrapper.findAndSetDefault(namespaces, bootstrapper)
                    .takeUnless { it is EmptyNamespace }
                    ?.let { logger.debug("Bootstrapper namespace founded: {}", it) }
            }
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
