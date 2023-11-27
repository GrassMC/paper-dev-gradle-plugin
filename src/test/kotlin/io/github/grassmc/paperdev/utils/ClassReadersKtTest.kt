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

import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.tasks.PaperPluginYmlTask
import org.gradle.api.DefaultTask
import java.util.*
import kotlin.reflect.jvm.jvmName
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ClassReadersKtTest {
    @Test
    fun `readBaseClasses should return superclass and interfaces`() {
        val byteCodes = DefaultTask::class.java.bytes()
        val baseClasses = readBaseClasses(byteCodes)
        assertContentEquals(listOf("org/gradle/api/internal/AbstractTask", "org/gradle/api/Task"), baseClasses)
    }

    @Test
    fun `readBaseClasses should return only superclass`() {
        val byteCodes = PaperPluginYmlTask::class.java.bytes()
        val baseClasses = readBaseClasses(byteCodes)
        assertContentEquals(listOf("org/gradle/api/DefaultTask"), baseClasses)
    }

    @Test
    fun `readBaseClasses should return only interfaces`() {
        val byteCodes = PaperPluginYml::class.java.bytes()
        val baseClasses = readBaseClasses(byteCodes)
        assertContentEquals(listOf("io/github/grassmc/paperdev/dsl/PermissionContainer"), baseClasses)
    }

    @Test
    fun `readBaseClasses should return empty list`() {
        val byteCodes = Object::class.java.bytes()
        val baseClasses = readBaseClasses(byteCodes)
        assertContentEquals(emptyList(), baseClasses)
    }

    @Test
    fun `readBaseClasses should return superclass and interfaces for nested class`() {
        val byteCodes = AbstractMap.SimpleEntry::class.java.bytes()
        val baseClasses = readBaseClasses(byteCodes)
        assertContentEquals(listOf("java/util/Map\$Entry", "java/io/Serializable"), baseClasses)
    }

    @Test
    fun `isNestedClass should return true for nested class`() {
        assert(isNestedClass(Map.Entry::class.jvmName))
    }

    @Test
    fun `isNestedClass should return false for top-level class`() {
        assert(!isNestedClass(Object::class.jvmName))
    }

    private fun Class<*>.bytes() = getResourceAsStream("/${name.replace('.', '/')}.class")!!.readBytes()
}
