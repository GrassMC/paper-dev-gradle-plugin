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

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class PaperLibrariesJsonTask : DefaultTask() {
    @get:Input
    abstract val librariesRootComponent: Property<ResolvedComponentResult>

    @get:OutputFile
    abstract val paperLibrariesJson: RegularFileProperty

    @TaskAction
    fun generate() {
        val librariesJson = buildMap {
            project.repositories
                .collectMavenRepositoriesWithoutCentral()
                .associate { it.name to it.url.toString() }
                .also { put("repositories", it) }
                .also {
                    logger.debug("Collected repository without Maven Central:")
                    it.forEach { repos ->
                        logger.debug("    - ${repos.key}: ${repos.value}")
                    }
                }

            librariesRootComponent
                .get()
                .collectModuleVersions()
                .also { put("dependencies", it) }
                .also {
                    logger.debug("Collected root dependencies:")
                    it.forEach { d ->
                        logger.debug("    - $d")
                    }
                }
        }

        JsonOutput.toJson(librariesJson)
            .also { logger.debug("Generated JSON: $it") }
            .also {
                paperLibrariesJson.asFile.get().writeText(it)
                logger.debug("Paper libraries JSON file generated at: ${paperLibrariesJson.get().asFile.path}")
            }
    }

    private fun RepositoryHandler.collectMavenRepositoriesWithoutCentral() = filterIsInstance<MavenArtifactRepository>()
        .filterNot { it.url.toString() == RepositoryHandler.MAVEN_CENTRAL_URL }

    private fun ResolvedComponentResult.collectModuleVersions() = dependencies
        .mapNotNull { (it as? ResolvedDependencyResult)?.selected?.moduleVersion }
        .map { it.toString() }
        .distinct()
}
