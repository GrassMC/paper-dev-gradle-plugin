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

package io.github.grassmc.paperdev.dsl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonValue
import io.github.grassmc.paperdev.namespace.PluginNamespace
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

@JsonPropertyOrder(
    "name",
    "main",
    "bootstrapper",
    "loader",
    "provides",
    "has-open-classloader",
    "version",
    "description",
    "authors",
    "contributors",
    "website",
    "prefix",
    "load",
    "defaultPerm",
    "permissions",
    "api-version",
    "dependencies",
)
class PaperPluginYml(@JsonIgnore private val project: Project) : PermissionContainer {
    @Input
    val name = project.objects.property<String>()

    @Input
    val version = project.objects.property<String>()

    @Input
    val main = project.objects.property<PluginNamespace>()

    @Input
    val apiVersion = project.objects.property<ApiVersion>()

    @Input
    @Optional
    val description = project.objects.property<String>()

    @Input
    @Optional
    val authors = project.objects.listProperty<String>()

    @Input
    @Optional
    val contributors = project.objects.listProperty<String>()

    @Input
    @Optional
    val website = project.objects.property<String>()

    @Input
    @Optional
    val bootstrapper = project.objects.property<PluginNamespace>()

    @Input
    @Optional
    val loader = project.objects.property<PluginNamespace>()

    @Input
    @Optional
    val provides = project.objects.listProperty<String>()

    @Input
    @Optional
    val hasOpenClassloader = project.objects.property<Boolean>()

    @Input
    @Optional
    val prefix = project.objects.property<String>()

    @Input
    @Optional
    val load = project.objects.property<PluginLoadOrder>()

    override val defaultPermission = project.objects.property<PermissionDefault>()

    override val permissions = project.container<Permission>()

    @Nested
    val dependencies: PluginDependencies = PluginDependencies(project)

    fun dependencies(action: PluginDependencies.() -> Unit) = action(dependencies)

    enum class ApiVersion(@JsonValue val version: String) {
        V1_19("1.19"),
        V1_20("1.20");

        companion object {
            val Default = V1_19
        }
    }

    enum class PluginLoadOrder {
        STARTUP,
        POSTWORLD
    }
}
