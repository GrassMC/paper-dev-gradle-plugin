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

package io.github.grassmc.paperdev.sample

import org.bukkit.plugin.java.JavaPlugin

class SampleKotlinPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("Hello, world!")
        server.scheduler.runTaskTimer(this, Task(), 0, 20)
        BukkitTask().runTaskTimer(this, 0, 20)
    }

    override fun onDisable() {
        logger.info("Goodbye, world!")
    }

    class Task : Runnable {
        override fun run() {
            println("Hello, world!")
        }
    }
}
