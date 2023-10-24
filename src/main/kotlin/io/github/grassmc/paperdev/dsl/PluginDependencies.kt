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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.container

class PluginDependencies(project: Project) {
    @Nested
    val bootstrap = project.container<Dependency>()

    @Nested
    val server = project.container<Dependency>()

    fun bootstrap(action: NamedDomainObjectContainer<Dependency>.() -> Unit) = action(bootstrap)

    fun server(action: NamedDomainObjectContainer<Dependency>.() -> Unit) = action(server)
}

data class Dependency(@JsonIgnore val name: String) {
    @Input
    @Optional
    var load: LoadOrder = LoadOrder.OMIT

    @Input
    @Optional
    var required: Boolean = true

    @Input
    @Optional
    var joinClasspath: Boolean = true

    enum class LoadOrder {
        BEFORE,
        AFTER,
        OMIT
    }
}
