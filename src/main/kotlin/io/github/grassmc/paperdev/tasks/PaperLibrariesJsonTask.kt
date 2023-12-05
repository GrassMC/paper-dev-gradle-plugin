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
import io.github.grassmc.paperdev.PaperDevGradlePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

/**
 * Generates a json file contains repositories and dependencies in the project.
 */
@CacheableTask
abstract class PaperLibrariesJsonTask : DefaultTask() {
    /**
     * The resolved component result from a [org.gradle.api.artifacts.Configuration].
     */
    @get:Input
    abstract val resolvedComponentResult: Property<ResolvedComponentResult>

    /**
     * The output file to write the generated json to.
     */
    @get:OutputFile
    abstract val paperLibrariesJson: RegularFileProperty

    @TaskAction
    fun generate() {
        val reposWithoutMavenCentral = collectRepositoriesWithoutMavenCentral()
        val moduleVersions = collectDependencies()

        val librariesJson = mapOf(
            "repositories" to reposWithoutMavenCentral,
            "dependencies" to moduleVersions
        )
        writeJsonToFile(JsonOutput.toJson(librariesJson))
    }

    private fun collectRepositoriesWithoutMavenCentral() = project.repositories
        .collectMavenRepositoriesWithoutCentral()
        .associate { it.name to it.url.toString() }
        .also { logCollectedData("repository without Maven Central", it.values) }

    private fun collectDependencies() = resolvedComponentResult
        .get()
        .collectModuleVersions()
        .also { logCollectedData("root dependencies", it) }

    private fun writeJsonToFile(json: String) {
        logger.debug("Generated JSON: $json")
        paperLibrariesJson.asFile.get().writeText(json)
        logger.debug("Paper libraries JSON file generated at: ${paperLibrariesJson.get().asFile.path}")
    }

    private fun <T> logCollectedData(logDescription: String, data: Collection<T>) {
        logger.debug("Collected $logDescription:")
        for (it in data) {
            logger.debug("    - {}", it)
        }
    }

    private fun RepositoryHandler.collectMavenRepositoriesWithoutCentral() = filterIsInstance<MavenArtifactRepository>()
        .filterNot { it.url.toString() == RepositoryHandler.MAVEN_CENTRAL_URL }

    private fun ResolvedComponentResult.collectModuleVersions() = dependencies
        .mapNotNull { (it as? ResolvedDependencyResult)?.selected?.moduleVersion }
        .map { it.toString() }
        .distinct()

    companion object {
        internal const val DEFAULT_NAME = "paperLibrariesJson"
    }
}

internal fun Project.registerPaperLibrariesJsonTask() =
    tasks.register<PaperLibrariesJsonTask>(PaperLibrariesJsonTask.DEFAULT_NAME) {
        group = PaperDevGradlePlugin.TASK_GROUP
        description = "Generates a json file contains repositories and dependencies in the project."

        resolvedComponentResult.convention(paperLibsRootResolution)
        paperLibrariesJson.convention(layout.buildDirectory.file(DEFAULT_PAPER_LIBRARIES_JSON_PATH))
    }

private val Project.paperLibsRootResolution
    get() = configurations
        .named(PaperDevGradlePlugin.PAPER_LIBS_CONFIGURATION_NAME)
        .map { it.incoming.resolutionResult.root }

internal const val DEFAULT_PAPER_LIBRARIES_JSON_PATH =
    "${PaperDevGradlePlugin.PAPER_DEV_DIR}/${PaperLibrariesJsonTask.DEFAULT_NAME}/paper-libraries.json"
