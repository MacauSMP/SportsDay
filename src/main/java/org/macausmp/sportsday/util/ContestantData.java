package org.macausmp.sportsday.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a contestant data.
 */
public final class ContestantData implements PlayerHolder {
    private final UUID uuid;
    private final int number;
    private int score;
    private boolean removed;

    public ContestantData(UUID uuid, int number) {
        this(uuid, number, 0);
    }

    public ContestantData(UUID uuid, int number, int score) {
        this.uuid = uuid;
        this.number = number;
        this.score = score;
    }

    /**
     * Gets the UUID of the {@link Player}.
     * @return {@link Player#getUniqueId()}
     */
    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the name of the {@link Player}.
     * @return {@link Player#getName()}
     */
    public String getName() {
        return getOfflinePlayer().getName();
    }

    /**
     * Gets the {@link #number} of the {@link Player}.
     * @return {@link Player} {@link #number}
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the {@link #score} of the {@link Player}.
     * @return {@link Player} {@link #score}
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the {@link #score} of the {@link Player}.
     * @param score new {@link #score} set for the {@link Player}
     */
    public void setScore(int score) {
        this.score = score;
        if (this.score < 0) this.score = 0;
    }

    /**
     * Add the value of the {@link #score}.
     * @param score value to increase of the {@link #score}
     */
    public void addScore(int score) {
        this.score += score;
        if (this.score < 0) this.score = 0;
    }

    /**
     * Returns {@code True} if this {@link ContestantData} has been marked for removal.
     * @return {@code True} if this {@link ContestantData} is removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Mark the {@link ContestantData} removal.
     */
    public void remove() {
        if (removed) return;
        removed = true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
