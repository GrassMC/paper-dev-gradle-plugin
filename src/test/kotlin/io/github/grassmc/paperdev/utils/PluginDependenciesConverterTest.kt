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

package io.github.grassmc.paperdev.utils

import io.github.grassmc.paperdev.dsl.Dependency
import io.github.grassmc.paperdev.dsl.PluginDependencies
import io.github.grassmc.paperdev.withProject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PluginDependenciesConverterTest {
    @Test
    fun `should return map with two keys server and bootstrap`() {
        withProject {
            val dependencies = PluginDependencies(project)
            val map = PluginDependenciesConverter.convert(dependencies)
            assertEquals(2, map.size)
            assertTrue(map.containsKey("server"))
            assertTrue(map.containsKey("bootstrap"))
        }
    }

    @Test
    fun `should return map of map dependencies`() {
        withProject {
            val dependencies = PluginDependencies(project)
            dependencies.server {
                create("test")
            }
            dependencies.bootstrap {
                create("test")
            }

            val map = PluginDependenciesConverter.convert(dependencies)
            val expected = mapOf(
                "server" to mapOf("test" to Dependency("test")), "bootstrap" to mapOf("test" to Dependency("test"))
            )
            assertContentEquals(expected.values, map.values)
        }
    }
}
