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
import com.fasterxml.jackson.annotation.JsonProperty
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

enum class PermissionDefault {
    TRUE,
    FALSE,
    OP,
    NOT_OP
}

interface PermissionContainer {
    @get:[Input Optional JsonProperty("defaultPerm")]
    val defaultPermission: Property<PermissionDefault>

    @get:Nested
    val permissions: NamedDomainObjectContainer<Permission>

    fun permissions(action: NamedDomainObjectContainer<Permission>.() -> Unit) = action(permissions)
}

data class Permission(@JsonIgnore @Input val name: String) {
    @Input
    @Optional
    var description: String? = null

    @Input
    @Optional
    var default: PermissionDefault? = null

    @Input
    @Optional
    var children: Map<String, Boolean> = emptyMap()
}
