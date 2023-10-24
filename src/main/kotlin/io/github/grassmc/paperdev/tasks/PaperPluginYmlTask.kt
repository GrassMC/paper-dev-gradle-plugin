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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class PaperPluginYmlTask : DefaultTask() {
    @get:Nested
    abstract val pluginYml: Property<PaperPluginYml>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val pluginYmlFile = outputDir.file(PAPER_PLUGIN_YML_FILENAME).get().asFile

        createYamlMapper().writeValueAsString(pluginYml.get()).also {
            pluginYmlFile.writeText(it)
        }
    }

    private fun createYamlMapper() = YAMLMapper
        .builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        .build()
        .registerKotlinModule()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)

    companion object {
        private const val PAPER_PLUGIN_YML_FILENAME = "paper-plugin.yml"
    }
}
