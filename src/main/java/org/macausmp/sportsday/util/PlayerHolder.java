package org.macausmp.sportsday.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerHolder {
    /**
     * Get the uuid of the player
     * @return The player's uuid
     */
    @NotNull
    UUID getUUID();

    /**
     * Get the player
     * @return The player
     */
    default Player getPlayer() {
        return getOfflinePlayer().getPlayer();
    }

    /**
     * Get the offline player
     * @return The offline player
     */
    @NotNull
    default OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getUUID());
    }

    /**
     * Checks if this player is currently online
     * @return true if they are online
     * @see OfflinePlayer#isOnline()
     */
    default boolean isOnline() {
        return getOfflinePlayer().isOnline();
    }
}
