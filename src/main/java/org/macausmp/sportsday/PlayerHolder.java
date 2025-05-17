package org.macausmp.sportsday;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerHolder {
    /**
     * Gets the uuid of the player.
     * @return the player's uuid
     */
    @NotNull
    UUID getUUID();

    /**
     * Gets the player.
     * @return the player
     */
    default Player getPlayer() {
        return getOfflinePlayer().getPlayer();
    }

    /**
     * Gets the offline player.
     * @return the offline player
     */
    @NotNull
    default OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getUUID());
    }

    /**
     * Checks if this player is currently online.
     * @return {@code True} if they are online
     * @see OfflinePlayer#isOnline()
     */
    default boolean isOnline() {
        return getOfflinePlayer().isOnline();
    }
}
