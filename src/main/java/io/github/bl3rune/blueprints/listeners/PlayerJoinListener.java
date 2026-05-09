package io.github.bl3rune.blueprints.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import net.kyori.adventure.text.Component;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Code to execute when a player joins the server
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!GlobalConfig.isUpdateAvailableMessageEnabled()) {
            return;
        }
        List<Component> updates = Blueprints.getUpdateMessages();
        if (player.hasPermission("blu3print.update-available-message") && !updates.isEmpty()) {
            updates.forEach(um -> player.sendMessage(um));
        }
    }
}
