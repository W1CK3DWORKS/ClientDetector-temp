/*
 * This file is part of ClientDetector - https://github.com/Sportkanone123/ClientDetector
 * Copyright (C) 2021 Sportkanone123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sportkanone123.clientdetector.spigot.manager;

import com.tcoded.folialib.FoliaLib;
import de.sportkanone123.clientdetector.spigot.ClientDetector;
import de.sportkanone123.clientdetector.spigot.api.events.ClientDetectedEvent;
import de.sportkanone123.clientdetector.spigot.bungee.DataType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AlertsManager {

    private static FoliaLib foliaLib = ClientDetector.getFoliaLib();
    private static FileConfiguration spigot;

    public static List<Player> disabledNotifications = new ArrayList<Player>();
    public static boolean limitedNotifications = true;
    public static boolean crossServerNotifications = false;
    public static List<UUID> firstDetection = new ArrayList<UUID>();

    public static List<String> modWarningList = new ArrayList<String>();

    public static void load(){
        limitedNotifications = ConfigManager.getConfig("config").getBoolean("alerts.limitNotifications");
        crossServerNotifications = ConfigManager.getConfig("config").getBoolean("alerts.crossServerNotifications");

        try {
            File spigotFile = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", "spigot.yml"));
            spigot = YamlConfiguration.loadConfiguration(spigotFile);
        }catch (Exception e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&4ERROR&7) &cSpigot.yml file not found!"));
        }
    }

    public static void handleClientDetection(Player player){
        if(!firstDetection.contains(player.getUniqueId())){
            firstDetection.add(player.getUniqueId());

            long waitTicks = 200; //4 ticks
            if (spigot.getBoolean("settings.bungeecord")) waitTicks = 5000; //100 ticks

            foliaLib.getImpl().runLater(() -> {
                if(ClientDetector.playerClient.get(player.getUniqueId()) != null)
                    ClientManager.handleDetection(player, ClientDetector.playerClient.get(player.getUniqueId()));

                foliaLib.getImpl().runAsync(() -> {
                    Bukkit.getPluginManager().callEvent(new ClientDetectedEvent(true, player, ClientDetector.playerClient.get(player.getUniqueId())));
                });

                if(ClientDetector.playerClient.get(player.getUniqueId()) != null){
                    if(ConfigManager.getConfig("config").getBoolean("discord.limitNotifications") == true) {
                        if (!ClientDetector.playerClient.get(player.getUniqueId()).equalsIgnoreCase("Vanilla (Undetectable)")) {
                            DiscordManager.handle(player, ClientDetector.playerClient.get(player.getUniqueId()));
                        }
                    }else{
                        DiscordManager.handle(player, ClientDetector.playerClient.get(player.getUniqueId()));
                    }
                }

                if(ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
                    for(Player player1 : Bukkit.getOnlinePlayers()){
                        if(!disabledNotifications.contains(player1.getName()) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission")) && !(ConfigManager.getConfig("config").getBoolean("bungee.enableBungeeClient") && crossServerNotifications)){
                            if(limitedNotifications){
                                if(!ClientDetector.playerClient.get(player.getUniqueId()).equalsIgnoreCase("Vanilla (Undetectable)")){
                                    if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) == null) {
                                        player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId()))));
                                    }else if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) != null) {
                                        player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId())).replace("%client_version%", ClientDetector.clientVersion.get(player.getUniqueId()))));
                                    }
                                }
                            }else{
                                if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) == null) {
                                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId()))));
                                }else if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) != null) {
                                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId())).replace("%client_version%", ClientDetector.clientVersion.get(player.getUniqueId()))));
                                }else{
                                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", "Vanilla (Undetectable)")));
                                }
                            }
                        }
                    }

                    if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) == null) {
                        sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId()))));
                    }else if(ClientDetector.playerClient.get(player.getUniqueId()) != null && ClientDetector.clientVersion.get(player.getUniqueId()) != null) {
                        sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player.getUniqueId())).replace("%client_version%", ClientDetector.clientVersion.get(player.getUniqueId()))));
                    }else{
                        sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", "Vanilla (Undetectable)")));
                    }
                }
            }, waitTicks, TimeUnit.MILLISECONDS);
        }
    }

    public static void handleModlistDetection(Player player, String modName){
        if(modWarningList.contains(modName) && ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
            for(Player player1 : Bukkit.getOnlinePlayers()) {
                if (!disabledNotifications.contains(player1.getName()) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission"))) {
                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.moddetectionmessage").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%mod_name%", modName)));
                }
            }
            sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.moddetectionmessage").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%mod_name%", modName)));
        }
    }

    public static void handleGeyserDetection(Player player){
        if(ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
            for(Player player1 : Bukkit.getOnlinePlayers()) {
                if (!limitedNotifications && !disabledNotifications.contains(player1) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission"))) {
                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.geyserdetectionmessage").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString())));
                }
            }
            sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.geyserdetectionmessage").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString())));
        }
    }

    public static void sendCrossServer(Player player, String message){
        if(ClientDetector.bungeeManager != null && crossServerNotifications && ConfigManager.getConfig("config").getBoolean("bungee.enableBungeeClient")){
            ClientDetector.bungeeManager.syncList(DataType.CROSS_SERVER_MESSAGE, message);
        }
    }
    public static void handleCrossServer(String message, String serverName){
        if(crossServerNotifications && ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
            for(Player player1 : Bukkit.getOnlinePlayers()) {
                if (!disabledNotifications.contains(player1.getName()) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission"))) {
                    if (limitedNotifications) {
                        if(!message.contains("Vanilla (Undetectable)"))
                            player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagecrossserver").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%server_name%", serverName).replace("%cross_server_message%", message)));
                    }else
                        player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagecrossserver").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%server_name%", serverName).replace("%cross_server_message%", message)));
                }
            }
        }
    }
}
