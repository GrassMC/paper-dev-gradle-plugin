package io.github.grassmc.paperdev.sample;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SamplePaperPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Hello, Paper!");
        getServer().getPluginManager().registerEvents(new SampleListener(), this);
        getServer().getScheduler().runTaskTimer(this, new SampleTask(), 0, 20);
        getServer().getScheduler().runTaskTimer(this, new InheritedSampleTask(), 0, 20);
        new LaterTask().runTaskLater(this, 10L);
    }

    class LaterTask extends BukkitRunnable {
        @Override
        public void run() {
            getLogger().info("Hello, Paper!");
        }
    }
}
