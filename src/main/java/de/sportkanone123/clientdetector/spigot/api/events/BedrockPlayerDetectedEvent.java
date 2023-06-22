package de.sportkanone123.clientdetector.spigot.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class BedrockPlayerDetectedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final UUID playerUUID;

    public BedrockPlayerDetectedEvent(boolean isAsync, Player player, UUID playerUUID) {
        super(isAsync);
        this.player = player;
        this.playerUUID = playerUUID;
    }

    @Deprecated
    public BedrockPlayerDetectedEvent(Player player, UUID playerUUID) {
        this.player = player;
        this.playerUUID = playerUUID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
