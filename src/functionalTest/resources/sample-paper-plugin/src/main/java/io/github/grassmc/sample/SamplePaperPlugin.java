package io.github.grassmc.paperdev.sample;

import org.bukkit.plugin.java.JavaPlugin;

class SamplePaperPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Hello, PaperDev!");
    }

    class Nested implements Runnable {
        @Override
        public void run() {
            getLogger().info("Hello, PaperDev!");
        }
    }
}
