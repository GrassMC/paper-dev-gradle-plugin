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

import io.github.grassmc.paperdev.dsl.Permission
import io.github.grassmc.paperdev.withProject
import org.gradle.kotlin.dsl.container
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs


class ContainerAsMapConverterTest {
    @Test
    fun `convert should return values map of container`() {
        withProject {
            val container = container(Permission::class)
            container.create("test") {
                description = "test"
            }
            container.create("test2") {
                description = "test2"
            }
            val map = ContainerAsMapConverter.convert(container)
            val expected = mapOf(
                "test" to Permission("test").apply {
                    description = "test"
                },
                "test2" to Permission("test2").apply {
                    description = "test2"
                }
            )
            assertIs<Map<String, Permission>>(map)
            assertContentEquals(expected.values, map.values)
        }
    }
}
