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

import com.fasterxml.jackson.databind.util.StdConverter
import io.github.grassmc.paperdev.dsl.PluginDependencies
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

object ContainerAsMapConverter : StdConverter<NamedDomainObjectContainer<*>, Map<String, *>>() {
    override fun convert(value: NamedDomainObjectContainer<*>): Map<String, *> = value.asMap
}

object PropertyAsValueConverter : StdConverter<Property<*>, Any?>() {
    override fun convert(value: Property<*>): Any? = value.orNull
}

object ListPropertyAsListConverter : StdConverter<ListProperty<*>, List<*>?>() {
    override fun convert(value: ListProperty<*>): List<*>? = value.orNull
}

object PluginDependenciesConverter : StdConverter<PluginDependencies, Map<String, *>>() {
    override fun convert(value: PluginDependencies): Map<String, *> = mapOf(
        "bootstrap" to value.bootstrap.asMap,
        "server" to value.server.asMap
    )
}
