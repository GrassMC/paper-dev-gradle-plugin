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

import io.github.grassmc.paperdev.withProject
import org.gradle.kotlin.dsl.property
import kotlin.test.Test
import kotlin.test.assertEquals


class PropertyAsValueConverterTest {
    @Test
    fun `should return value of property`() {
        withProject {
            val property = objects.property<String>()
            property.set("test")

            val value = PropertyAsValueConverter.convert(property)
            assertEquals("test", value)
        }
    }

    @Test
    fun `should return null if property is not set`() {
        withProject {
            val property = objects.property<String>()

            val value = PropertyAsValueConverter.convert(property)
            assertEquals(null, value)
        }
    }
}
