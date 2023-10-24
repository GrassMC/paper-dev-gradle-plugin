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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object YamlSerializer {
    private val mapper by lazy {
        createMapper().registerModule(
            SimpleModule()
                .addConverter(ContainerAsMapConverter)
                .addConverter(PropertyAsValueConverter)
                .addConverter(ListPropertyAsListConverter)
                .addConverter(PluginDependenciesConverter)
        )
    }

    fun serialize(value: Any): String = mapper.writeValueAsString(value)

    private fun createMapper() = YAMLMapper
        .builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
        .build()
        .registerKotlinModule()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)

    private inline fun <reified T> SimpleModule.addConverter(converter: StdConverter<T, *>) =
        addSerializer(T::class.java, StdDelegatingSerializer(converter))
}
