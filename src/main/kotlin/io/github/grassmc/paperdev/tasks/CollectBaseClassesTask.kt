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

import io.github.grassmc.paperdev.utils.isNestedClass
import io.github.grassmc.paperdev.utils.readBaseClasses
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeLines

@CacheableTask
abstract class CollectBaseClassesTask : DefaultTask() {
    @get:CompileClasspath
    @get:SkipWhenEmpty
    abstract val classes: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val skipNestedClass: Property<Boolean>

    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty

    @TaskAction
    fun collect(changes: InputChanges) {
        val skipNestedClass = skipNestedClass.getOrElse(true)
        logger.debug("Skip nested classes: $skipNestedClass")
        val readClasses = mutableSetOf<String>()
        changes
            .getFileChanges(classes)
            .asSequence()
            .filter { it.fileType == FileType.FILE }
            .filter { it.file.path.endsWith(CLASS_FILE_EXTENSION) }
            .forEach { change ->
                val className = change.normalizedPath.removeSuffix(CLASS_FILE_EXTENSION)
                if (skipNestedClass && isNestedClass(className)) {
                    return@forEach
                }
                if (className in readClasses) {
                    return@forEach
                }

                readClasses += className
                when (change.changeType) {
                    ChangeType.ADDED, ChangeType.MODIFIED -> readBaseClasses(change.file.readBytes())
                        .filterNot { skipNestedClass && isNestedClass(it) }
                        .takeIf { it.isNotEmpty() }
                        ?.let { destinationDir.path(className.namespace()).writeLines(it) }
                        .also { logger.debug("Collected base classes for $className") }

                    else -> destinationDir
                        .path(className.namespace())
                        .deleteIfExists()
                        .also { logger.debug("Removed base classes for $className") }
                }
            }
    }

    private fun DirectoryProperty.path(path: String) = file(path).get().asFile.toPath()

    private fun String.namespace(): String = this.replace('/', '.')

    companion object {
        internal const val DEFAULT_NAME = "collectBaseClasses"
        private const val CLASS_FILE_EXTENSION = ".class"
    }
}

internal fun Project.registerCollectBaseClassesTask() =
    tasks.register<CollectBaseClassesTask>(CollectBaseClassesTask.DEFAULT_NAME) {
        description = "Collects base classes from the project's classes directory."

        classes.from(compiledClasses())
        skipNestedClass.convention(true)
        destinationDir = temporaryDirFactory.create()
    }

private fun Project.compiledClasses() = extensions
    .getByType<SourceSetContainer>()
    .named(SourceSet.MAIN_SOURCE_SET_NAME)
    .map { it.output.classesDirs }
