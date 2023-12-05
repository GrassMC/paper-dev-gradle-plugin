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

import io.github.grassmc.paperdev.PaperDevGradlePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.register
import org.intellij.lang.annotations.Language

/**
 * Generates a plugin loader java source that will load the plugin libraries.
 */
@CacheableTask
abstract class GeneratePluginLoaderTask : DefaultTask() {
    /**
     * Whether to disable the generation of the plugin loader.
     */
    @get:Input
    @get:Optional
    abstract val disableGenerate: Property<Boolean>

    /**
     * The root directory of the generated sources.
     */
    @get:OutputDirectory
    abstract val generatedDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        if (disableGenerate.getOrElse(false)) return
        generatedDirectory.get().asFile.run {
            val pluginClassLoader = resolve(PAPER_LIBS_LOADER_JAVA_PATH)
            pluginClassLoader.parentFile.mkdirs()
            pluginClassLoader.writeText(PAPER_LIBS_LOADER_JAVA_TEMPLATE)
        }
    }

    companion object {
        internal const val DEFAULT_NAME = "generatePluginLoader"
    }
}

internal fun Project.registerGeneratePluginLoaderTask() =
    tasks.register<GeneratePluginLoaderTask>(GeneratePluginLoaderTask.DEFAULT_NAME) {
        group = PaperDevGradlePlugin.TASK_GROUP
        description = "Generates a plugin loader java source that will load the plugin libraries."

        generatedDirectory.convention(layout.buildDirectory.dir(DEFAULT_GENERATED_DIR))
    }

internal const val DEFAULT_GENERATED_DIR = "${PaperDevGradlePlugin.PAPER_DEV_DIR}/generatedPluginLoader"
internal const val PAPER_LIBS_LOADER_JAVA_PATH = "io/github/grassmc/paperdev/loader/PaperLibsLoader.java"

@Language("java")
internal const val PAPER_LIBS_LOADER_JAVA_TEMPLATE = """package io.github.grassmc.paperdev.loader;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class PaperLibsLoader implements PluginLoader {
    public static final String PAPER_LIBRARIES_JSON_CLASSPATH = "/paper-libraries.json";

    private @Nullable Reader getPaperLibrariesJson() {
        var in = getClass().getResourceAsStream(PAPER_LIBRARIES_JSON_CLASSPATH);
        if (in == null) return null;
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private @Nullable Libraries getPaperLibraries() {
        try (var reader = getPaperLibrariesJson()) {
            if (reader != null) return new Gson().fromJson(reader, Libraries.class);
            return null;
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var libraries = getPaperLibraries();
        if (libraries == null) return;
        var resolver = new MavenLibraryResolver();
        libraries.collectRemoteRepositories().forEach(resolver::addRepository);
        libraries.collectDependencies().forEach(resolver::addDependency);
        classpathBuilder.addLibrary(resolver);
    }

    record Libraries(Map<String, String> repositories, List<String> dependencies) {
        private static RemoteRepository createRemoteRepository(Map.@NotNull Entry<String, String> repository) {
            return new RemoteRepository.Builder(repository.getKey(), "default", repository.getValue()).build();
        }

        private static @NotNull Dependency createDependency(String coords) {
            return new Dependency(new DefaultArtifact(coords), null);
        }

        private List<RemoteRepository> collectRemoteRepositories() {
            return repositories.entrySet().stream().map(Libraries::createRemoteRepository).toList();
        }

        private List<Dependency> collectDependencies() {
            return dependencies.stream().map(Libraries::createDependency).toList();
        }
    }
}
"""
