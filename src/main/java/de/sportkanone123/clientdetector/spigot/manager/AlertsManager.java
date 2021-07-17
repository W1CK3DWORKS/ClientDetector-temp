package de.sportkanone123.clientdetector.spigot.manager;

import de.sportkanone123.clientdetector.spigot.ClientDetector;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.play.out.custompayload.WrappedPacketOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertsManager {
    public static List<Player> disabledNotifications = new ArrayList<Player>();
    public static boolean limitedNotifications = true;
    public static boolean crossServerNotifications = false;
    public static List<Player> firstDetection = new ArrayList<Player>();

    public static List<String> modWarningList = new ArrayList<String>();

    public static void load(){
        limitedNotifications = ConfigManager.getConfig("config").getBoolean("alerts.limitNotifications");
        crossServerNotifications = ConfigManager.getConfig("config").getBoolean("alerts.crossServerNotifications");
    }

    public static void handleClientDetection(Player player){
        if(!firstDetection.contains(player) && ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
            firstDetection.add(player);

            int waitTicks = 4;
            if(PacketEvents.get().getServerUtils().isBungeeCordEnabled()) waitTicks = 100;

            Bukkit.getScheduler().runTaskLater(ClientDetector.plugin, () -> {
               for(Player player1 : Bukkit.getOnlinePlayers()){
                   if(!disabledNotifications.contains(player1.getName()) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission"))){
                        if(limitedNotifications){
                            if(!ClientDetector.playerClient.get(player).equalsIgnoreCase("Vanilla Minecraft / Undetectable Client")){
                                if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) == null) {
                                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player))));
                                }else if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) != null) {
                                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player)).replace("%client_version%", ClientDetector.clientVersion.get(player))));
                                }
                            }
                        }else{
                            if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) == null) {
                                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player))));
                            }else if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) != null) {
                                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player)).replace("%client_version%", ClientDetector.clientVersion.get(player))));
                            }else{
                                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", "Vanilla Minecraft / Undetectable Client")));
                            }
                        }
                   }
               }


               if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) == null) {
                   sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player))));
               }else if(ClientDetector.playerClient.get(player) != null && ClientDetector.clientVersion.get(player) != null) {
                   sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", ClientDetector.playerClient.get(player)).replace("%client_version%", ClientDetector.clientVersion.get(player))));
               }else{
                   sendCrossServer(player, ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagewithoutversion").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString()).replace("%client_name%", "Vanilla Minecraft / Undetectable Client")));
               }
            }, waitTicks);
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
        if(crossServerNotifications){
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);

            try {
                msgout.writeUTF(Bukkit.getServer().getName());
                msgout.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.sendPluginMessage(ClientDetector.plugin, "clientdetector:sync", msgbytes.toByteArray());
        }
    }

    public static void handle(Player player, String channel, byte[] data){
        if(channel.equalsIgnoreCase("clientdetector:sync") && crossServerNotifications && ConfigManager.getConfig("config").getBoolean("alerts.enableNotifications")){
            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(data));

            String serverName = "";
            String message = "";
            try {
                serverName = msgin.readUTF();
                message = msgin.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!serverName.equalsIgnoreCase(Bukkit.getServer().getName())){
                for(Player player1 : Bukkit.getOnlinePlayers()) {
                    if (!disabledNotifications.contains(player1.getName()) && player1.hasPermission(ConfigManager.getConfig("config").getString("alerts.notificationPermission"))) {
                        if (limitedNotifications) {
                            if(!message.contains("Vanilla Minecraft / Undetectable Client"))
                                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagecrossserver").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%server_name%", serverName).replace("%cross_server_message%", message)));
                        }else
                            player1.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig("message").getString("detection.clientdetectionmessagecrossserver").replace("%prefix%", ConfigManager.getConfig("message").getString("prefix")).replace("%server_name%", serverName).replace("%cross_server_message%", message)));
                    }
                }
            }
        }
    }
}