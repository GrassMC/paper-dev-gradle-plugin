package $package;

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
        if (in == null) {
            return null;
        }

        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private @Nullable Libraries getPaperLibraries() {
        try (var reader = getPaperLibrariesJson()) {
            if (reader != null) {
                return new Gson().fromJson(reader, Libraries.class);
            }

            return null;
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var libraries = getPaperLibraries();
        if (libraries == null) {
            return;
        }

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
            return repositories
                    .entrySet()
                    .stream()
                    .map(Libraries::createRemoteRepository)
                    .toList();
        }

        private List<Dependency> collectDependencies() {
            return dependencies
                    .stream()
                    .map(Libraries::createDependency)
                    .toList();
        }
    }
}
