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

package io.github.grassmc.paperdev.dsl

import io.github.grassmc.paperdev.namespace.PluginNamespace
import io.github.grassmc.paperdev.utils.YamlSerializer
import io.github.grassmc.paperdev.withProject
import org.gradle.kotlin.dsl.assign
import kotlin.test.Test
import kotlin.test.assertEquals

class PaperPluginYmlSerializationTest {
    @Test
    fun `PaperPluginYml should be serialized`() {
        withProject {
            val pluginYml = PaperPluginYml(this).apply {
                name = "Test"
                version = "1.0.0"
                description = "Test plugin"
                main = PluginNamespace("io.github.grassmc.paperdev.TestPlugin")
                authors = listOf("GrassMC")
                contributors = listOf("GrassMC")
                website = "https://grassmc.github.io"
                bootstrapper = PluginNamespace("io.github.grassmc.paperdev.TestBootstrapper")
                loader = PluginNamespace("io.github.grassmc.paperdev.TestLoader")
                provides = listOf("Test")
                hasOpenClassloader = true
                prefix = "Test"
                load = PaperPluginYml.PluginLoadOrder.STARTUP
                apiVersion = PaperPluginYml.ApiVersion.V1_20
                defaultPermission = PermissionDefault.OP
                permissions {
                    create("perm.test1")
                    create("perm.test2").apply {
                        description = "Test permission"
                        default = PermissionDefault.TRUE
                    }
                    create("perm.test3").apply {
                        description = "Test permission 2"
                        default = PermissionDefault.NOT_OP
                        children = mapOf("test" to true)
                    }
                    create("test4").apply {
                        description = "Test permission 4"
                        default = PermissionDefault.FALSE
                        children = mapOf("test" to true, "test2" to false)
                    }
                }
                dependencies {
                    bootstrap {
                        create("Plugin1")
                        create("Plugin2").apply {
                            load = Dependency.LoadOrder.AFTER
                            required = true
                            joinClasspath = true
                        }
                    }
                    server {
                        create("Plugin3")
                        create("Plugin4").apply {
                            load = Dependency.LoadOrder.BEFORE
                            required = false
                        }
                        create("Plugin5").apply {
                            load = Dependency.LoadOrder.OMIT
                            joinClasspath = false
                        }
                    }
                }
            }
            val actual = YamlSerializer.serialize(pluginYml)
            val expected = """
                name: "Test"
                main: "io.github.grassmc.paperdev.TestPlugin"
                bootstrapper: "io.github.grassmc.paperdev.TestBootstrapper"
                loader: "io.github.grassmc.paperdev.TestLoader"
                provides:
                  - "Test"
                has-open-classloader: true
                version: "1.0.0"
                description: "Test plugin"
                authors:
                  - "GrassMC"
                contributors:
                  - "GrassMC"
                website: "https://grassmc.github.io"
                prefix: "Test"
                load: "STARTUP"
                defaultPerm: "OP"
                permissions:
                  perm.test1: {}
                  perm.test2:
                    description: "Test permission"
                    default: "TRUE"
                  perm.test3:
                    description: "Test permission 2"
                    default: "NOT_OP"
                    children:
                      test: true
                  test4:
                    description: "Test permission 4"
                    default: "FALSE"
                    children:
                      test: true
                      test2: false
                api-version: "1.20"
                dependencies:
                  bootstrap:
                    Plugin1: {}
                    Plugin2:
                      load: "AFTER"
                      required: true
                      join-classpath: true
                  server:
                    Plugin3: {}
                    Plugin4:
                      load: "BEFORE"
                      required: false
                    Plugin5:
                      load: "OMIT"
                      join-classpath: false

            """.trimIndent()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `should serialize PaperPluginYml with no configuration`() {
        withProject {
            val pluginYml = PaperPluginYml(this).apply {
                name = "Test"
                version = "1.0.0"
                main = PluginNamespace("io.github.grassmc.paperdev.TestPlugin")
                apiVersion = PaperPluginYml.ApiVersion.V1_20
            }
            val actual = YamlSerializer.serialize(pluginYml)
            val expected = """
                name: "Test"
                main: "io.github.grassmc.paperdev.TestPlugin"
                version: "1.0.0"
                api-version: "1.20"

            """.trimIndent()
            assertEquals(expected, actual)
        }
    }
}
