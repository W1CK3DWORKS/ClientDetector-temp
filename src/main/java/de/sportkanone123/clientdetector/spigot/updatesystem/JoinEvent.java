package de.sportkanone123.clientdetector.spigot.updatesystem;

import de.sportkanone123.clientdetector.spigot.ClientDetector;
import de.sportkanone123.clientdetector.spigot.manager.ColorUtils;
import de.sportkanone123.clientdetector.spigot.manager.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    FileConfiguration config = ConfigManager.getConfig("config");
    FileConfiguration messagesConfig = ConfigManager.getConfig("message");

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("clientdetector.update") || player.hasPermission("clientdetector.*") || player.isOp()) {
            if (config.getBoolean("plugin-update-notifications.enabled")){
                new UpdateChecker(90375).getVersion(version -> {
                    try {
                        if (!(ClientDetector.getPlugin().getDescription().getVersion().equalsIgnoreCase(version))) {
                            player.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("update-available.1")));
                            player.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("update-available.2")));
                            player.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("update-available.3")));
                        }
                    }catch (NullPointerException e){
                        player.sendMessage(ColorUtils.translateColorCodes(messagesConfig.getString("Update-check-failure")));
                    }
                });
            }
        }
    }
}
