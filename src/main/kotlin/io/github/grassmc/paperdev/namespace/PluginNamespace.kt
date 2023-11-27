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

package io.github.grassmc.paperdev.namespace

/**
 * Represents a namespace which can be used to identify an entry point of some functionality,
 * such as a plugin main class, or a plugin loader class.
 */
sealed interface PluginNamespace

/**
 * Represents an empty namespace.
 * `findEntryNamespaces` task will resolve to this namespace if entry points are found.
 */
internal object EmptyNamespace : PluginNamespace

internal data class SpecifiedNamespace(val className: String) : PluginNamespace {
    override fun toString() = className
}

/**
 * Creates a [PluginNamespace] from the given full specified [className].
 */
fun PluginNamespace(className: String): PluginNamespace =
    if (className.isBlank())
        EmptyNamespace
    else
        SpecifiedNamespace(className)
