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

package de.sportkanone123.clientdetector.spigot;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.tcoded.folialib.FoliaLib;
import de.sportkanone123.clientdetector.spigot.bungee.BungeeManager;
import de.sportkanone123.clientdetector.spigot.client.Client;
import de.sportkanone123.clientdetector.spigot.clientcontrol.ClientControl;
import de.sportkanone123.clientdetector.spigot.command.Command;
import de.sportkanone123.clientdetector.spigot.forgemod.ModList;
import de.sportkanone123.clientdetector.spigot.hackdetector.HackDetector;
import de.sportkanone123.clientdetector.spigot.hackdetector.impl.AntiFastMath;
import de.sportkanone123.clientdetector.spigot.hackdetector.impl.ChatExploit;
import de.sportkanone123.clientdetector.spigot.listener.NetworkListener;
import de.sportkanone123.clientdetector.spigot.listener.PlayerListener;
import de.sportkanone123.clientdetector.spigot.listener.PluginMessageListener;
import de.sportkanone123.clientdetector.spigot.manager.*;
import de.sportkanone123.clientdetector.spigot.mod.Mod;
import de.sportkanone123.clientdetector.spigot.updatesystem.JoinEvent;
import de.sportkanone123.clientdetector.spigot.updatesystem.UpdateChecker;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class ClientDetector extends JavaPlugin {

    ConsoleCommandSender console = Bukkit.getConsoleSender();

    private static Plugin plugin;
    private static FoliaLib foliaLib;
    private static FloodgateApi floodgateApi;
    public static ArrayList<Client> CLIENTS = new ArrayList<Client>();
    public static ArrayList<Mod> MODS = new ArrayList<Mod>();

    public static HashMap<UUID, String> mcVersion = new HashMap<UUID, String>();
    public static HashMap<UUID, ModList> forgeMods = new HashMap<UUID, ModList>();
    public static HashMap<UUID, String> playerClient = new HashMap<UUID, String> ();
    public static HashMap<UUID, String> clientVersion = new HashMap<UUID, String> ();
    public static HashMap<UUID, String> connectedBedrockPlayers = new HashMap<>();
    public static HashMap<UUID, ArrayList<String>> playerMods = new HashMap<UUID, ArrayList<String>> ();
    public static HashMap<UUID, ArrayList<String>> playerLabymodMods = new HashMap<UUID, ArrayList<String>> ();
    public static HashMap<UUID, ArrayList<String>> playerCommandsQueue = new HashMap<UUID, ArrayList<String>>();
    public static BungeeManager bungeeManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .bStats(true)
                .debug(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        plugin = this;
        foliaLib = new FoliaLib((JavaPlugin) plugin);

        if (foliaLib.isSpigot()||foliaLib.isUnsupported()){
            PaperLib.suggestPaper(this);
        }

        new MetricsManager(this, 10745);

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aVersion&7) &aDetected Version &c" + Bukkit.getVersion()));
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aVersion&7) &aLoading settings for Version &c" + Bukkit.getVersion()));

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aProtocol&7) &aLoading protocols..."));
        PacketEvents.getAPI().getEventManager().registerListener(new NetworkListener());
        PacketEvents.getAPI().init();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "clientdetector:sync");
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "lunarclient:pm");

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "clientdetector:sync", new PluginMessageListener());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "clientdetector:fix", new PluginMessageListener());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "lunarclient:pm", new PluginMessageListener());

        if(PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2))
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "CB-Client", new PluginMessageListener());

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aConfig&7) &aLoading config(s)..."));
        saveDefaultConfig();

        getCommand("clientdetector").setExecutor(new Command());
        getCommand("client").setExecutor(new Command());
        getCommand("forge").setExecutor(new Command());
        getCommand("mods").setExecutor(new Command());

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new ClientControl(), this);
        this.getServer().getPluginManager().registerEvents(new JoinEvent(), this);

        if(ConfigManager.getConfig("config").getBoolean("hackdetector.chatexploit.enableChatExploit") || ConfigManager.getConfig("config").getBoolean("hackdetector.antifastmath.enableAntiFastMath"))
            Bukkit.getPluginManager().registerEvents(new HackDetector(), this);

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aDetection&7) &aLoading client detections..."));
        ClientManager.load();
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aDetection&7) &aLoading mod detections..."));
        ModManager.load();

        AlertsManager.load();

        DiscordManager.load();

        AntiFastMath.load();

        if(ConfigManager.getConfig("config").getBoolean("bungee.enableBungeeClient")){
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aDetection&7) &aLoading Bungee client..."));

            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "cd:bungee");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "cd:spigot", new PluginMessageListener());

            bungeeManager = new BungeeManager();
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("floodgate")||isFloodgateEnabled()){
            floodgateApi = FloodgateApi.getInstance();
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aFloodgateAPI&7) &aDetected FloodgateAPI " + Bukkit.getPluginManager().getPlugin("floodgate").getDescription().getVersion()));
        }

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("ViaVersion")){
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aViaVersion&7) &aDetected ViaVersion " + Bukkit.getPluginManager().getPlugin("ViaVersion").getDescription().getVersion()));
        }

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")){
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aProtocolLib&7) &aDetected ProtocolLib " + Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion()));
        }

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")){
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] (&aPlaceholderAPI&7) &aDetected PlaceholderAPI " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion()));
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && getConfig().getBoolean("placeholder.enablePlaceholder"))
            new PlaceholderManager().register();

        try {
            ConfigManager.loadConfig("message");
            ConfigManager.loadConfig("clientcontrol");

            if(PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17)){
                if(ConfigManager.optimizeConfig("config", "forge.simulateForgeHandshake", false))
                    console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] &cIMPORTANT NOTIFICATION: &aForge modlist detection for 1.17 - 1.20.2 is currently marked as UNSTABLE and therefore will be automatically disabled!!"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] &aStarted!"));

        //Check for available updates
        new UpdateChecker(90375).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("no-update-available.1")));
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("no-update-available.2")));
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("no-update-available.3")));
            }else {
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("update-available.1")));
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("update-available.2")));
                console.sendMessage(ColorUtils.translateColorCodes(ConfigManager.getConfig("message").getString("update-available.3")));
            }
        });
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7-----------------------------------------"));
        try {
            if (!ChatExploit.getTimerTask().isCancelled()){
                ChatExploit.getTimerTask().cancel();
            }
            console.sendMessage(ColorUtils.translateColorCodes("&7[&3ClientDetector&7] &3Background tasks have disabled successfully!"));
        }catch (Exception e){
            console.sendMessage(ColorUtils.translateColorCodes("&7[&3ClientDetector&7] &3Background tasks have disabled successfully!"));
        }

        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);

        ClientManager.unLoad();
        ModManager.unLoad();

        HandlerList.unregisterAll(this);

        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7-----------------------------------------"));
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&3ClientDetector&7] &aShutdown complete!"));
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7-----------------------------------------"));
    }

    public boolean isFloodgateEnabled() {
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public static FloodgateApi getFloodgateApi() {
        return floodgateApi;
    }
}
