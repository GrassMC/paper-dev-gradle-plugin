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

interface PluginNamespaceFinder {
    fun findFrom(namespaces: Iterable<Namespace>): Namespace?

    enum class Type(private vararg val parents: String) : PluginNamespaceFinder {
        MAIN("org.bukkit.plugin.java.JavaPlugin"),
        BOOTSTRAPPER("io.papermc.paper.plugin.bootstrap.PluginBootstrap"),
        LOADER("io.papermc.paper.plugin.loader.PluginLoader");

        override fun findFrom(namespaces: Iterable<Namespace>) = namespaces.firstOrNull { namespace ->
            namespace.superName in parents || namespace.interfaces.any { it in parents }
        }
    }
}
