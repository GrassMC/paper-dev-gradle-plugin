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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PluginNamespaceFinderTest {
    private val namespaceWithBases = mapOf(
        "io.github.grassmc.paperdev.testplugin.Main" to setOf("org/bukkit/plugin/java/JavaPlugin", "example/Example"),
        "io.github.grassmc.paperdev.testplugin.Loader" to setOf("io/papermc/paper/plugin/loader/PluginLoader"),
        "io.github.grassmc.paperdev.testplugin.Bootstrap" to setOf("io/papermc/paper/plugin/bootstrap/PluginBootstrap"),
        "io.github.grassmc.paperdev.testplugin.Command" to setOf("org/bukkit/command/CommandExecutor"),
        "io.github.grassmc.paperdev.testplugin.Listener" to setOf("org/bukkit/event/Listener"),
        "io.github.grassmc.paperdev.testplugin.TabCompleter" to setOf("org/bukkit/command/TabCompleter"),
    )

    @Test
    fun `should find entry for main namespace`() {
        val namespace = PluginNamespaceFinder.EntryFor.Main.find(namespaceWithBases)
        assertIs<SpecifiedNamespace>(namespace)
        assertEquals("io.github.grassmc.paperdev.testplugin.Main", namespace.className)
    }

    @Test
    fun `should not find entry for main namespace`() {
        val namespace = PluginNamespaceFinder.EntryFor.Main.find(emptyMap())
        assertIs<EmptyNamespace>(namespace)
    }

    @Test
    fun `should find entry for loader namespace`() {
        val namespace = PluginNamespaceFinder.EntryFor.Loader.find(namespaceWithBases)
        assertIs<SpecifiedNamespace>(namespace)
        assertEquals("io.github.grassmc.paperdev.testplugin.Loader", namespace.className)
    }

    @Test
    fun `should find entry for bootstrapper namespace`() {
        val namespace = PluginNamespaceFinder.EntryFor.Bootstrapper.find(namespaceWithBases)
        assertIs<SpecifiedNamespace>(namespace)
        assertEquals("io.github.grassmc.paperdev.testplugin.Bootstrap", namespace.className)
    }
}
