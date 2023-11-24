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

package io.github.grassmc.paperdev.namespace

import java.net.URL

internal fun interface PluginNamespaceFinder {
    fun find(namespacesWithBases: Map<String, Set<String>>): PluginNamespace

    companion object {
        private const val BASE_CLASSES_URL_PREFIX =
            "https://raw.githubusercontent.com/GrassMC/paper-dev-gradle-plugin/base-namespaces/"
    }

    /**
     * This enum is used to find the namespace of a plugin for different entry points.
     * This finder will read the base classes from the [baseClassesUrl] and compare them with the base classes of the
     * plugin to find the entry namespace.
     */
    enum class EntryFor(private val baseClassesUrl: String) : PluginNamespaceFinder {
        Main(BASE_CLASSES_URL_PREFIX + "main.txt"),
        Loader(BASE_CLASSES_URL_PREFIX + "loader.txt"),
        Bootstrapper(BASE_CLASSES_URL_PREFIX + "bootstrapper.txt");

        override fun find(namespacesWithBases: Map<String, Set<String>>): PluginNamespace {
            val baseClasses = URL(baseClassesUrl).readText().lines()
            val results = namespacesWithBases.filterValues { bases -> bases.any { it in baseClasses } }.keys
            return results.singleOrNull()?.let { PluginNamespace(it) } ?: EmptyNamespace
        }
    }
}
