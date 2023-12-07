package io.github.grassmc.paperdev.sample;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.jetbrains.annotations.NotNull;

public class CustomBootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLogger().info("Hello, Paper!");
    }
}
