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

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

class PaperPluginYml(@Transient private val project: Project) {
    val name = stringProperty()
    val version = stringProperty()
    val main = stringProperty()
    val apiVersion = property<ApiVersion>()
    val description = stringProperty()
    val authors = stringListProperty()
    val contributors = stringListProperty()
    val website = stringProperty()

    val bootstrapper = stringProperty()
    val loader = stringProperty()
    val provides = stringListProperty()
    val hasOpenClassloader = booleanProperty()

    val prefix = stringProperty()
    val load = property<PluginLoadOrder>()

    val defaultPermission = property<PermissionDefault>()
    val permissions = project.container<Permission>()

    val dependencies: PaperPluginDependencies = PaperPluginDependencies(project)

    fun permissions(action: NamedDomainObjectContainer<Permission>.() -> Unit) = action(permissions)

    fun dependencies(action: PaperPluginDependencies.() -> Unit) = action(dependencies)

    private inline fun <reified T> property() = project.objects.property<T>()

    private fun stringProperty() = project.objects.property<String>()

    private fun booleanProperty() = project.objects.property<Boolean>()

    private fun stringListProperty() = project.objects.listProperty<String>()

    enum class ApiVersion {
        V1_19,
        V1_20,
    }

    enum class PluginLoadOrder {
        STARTUP,
        POSTWORLD
    }

    enum class PermissionDefault {
        TRUE,
        FALSE,
        OP,
        NOT_OP
    }

    data class Permission(val name: String) {
        var description: String? = null
        var default: PermissionDefault? = null
        var children: Map<String, Boolean> = emptyMap()
    }
}
