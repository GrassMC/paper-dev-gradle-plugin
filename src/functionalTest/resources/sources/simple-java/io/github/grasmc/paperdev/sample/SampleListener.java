package io.github.grassmc.paperdev.sample;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SampleListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome to the server!");
    }
}
