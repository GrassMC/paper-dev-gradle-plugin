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

enum class PaperVersions(val version: String) {
    V1_19_3("1.19.3"),
    V1_20("1.20"),
    V1_20_1("1.20.1"),
    V1_20_2("1.20.2");

    fun toDependencyNotation() = "io.papermc.paper:paper-api:${version}-R0.1-SNAPSHOT"

    companion object {
        val Latest = values().last()
    }
}
