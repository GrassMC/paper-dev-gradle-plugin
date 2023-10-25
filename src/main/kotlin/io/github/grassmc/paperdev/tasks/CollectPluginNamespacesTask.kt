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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import java.io.File

@CacheableTask
abstract class CollectPluginNamespacesTask : DefaultTask() {
    @get:Input
    abstract val inheritedClassNames: ListProperty<String>

    @get:[InputFiles SkipWhenEmpty PathSensitive(PathSensitivity.RELATIVE)]
    abstract val classes: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun collect(changes: InputChanges) {
        changes
            .getFileChanges(classes)
            .asSequence()
            .map { it.file }
            .filter { it.exists() && it.isFile && it.extension == "class" }
            .mapNotNull { it.classReader().filterInherited() }
            .distinct()
            .joinToString("\n") { (className, inheritedClassName) ->
                "$className->$inheritedClassName"
            }
            .let { outputFile.get().asFile.writeText(it) }
    }

    private fun ClassReader.filterInherited() = listOf(superName, *interfaces)
        .map { it.namespace() }
        .firstOrNull { it in inheritedClassNames.get() }
        ?.let { className.namespace() to it }

    private fun File.classReader() = inputStream().use { ClassReader(it) }

    private fun String.namespace(): String = this.replace('/', '.')
}
