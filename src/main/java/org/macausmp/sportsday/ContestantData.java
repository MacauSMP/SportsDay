package org.macausmp.sportsday;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a contestant data.
 */
public final class ContestantData implements PlayerHolder {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    static final PersistentDataType<PersistentDataContainer, ContestantData> CONTESTANT_DATA_TYPE = new PersistentDataType<>() {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<ContestantData> getComplexType() {
            return ContestantData.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull ContestantData complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "uuid"), STRING, complex.uuid.toString());
            pdc.set(new NamespacedKey(PLUGIN, "number"), INTEGER, complex.number);
            pdc.set(new NamespacedKey(PLUGIN, "score"), INTEGER, complex.score);
            return pdc;
        }

        @Override
        public @NotNull ContestantData fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            int number = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "number"), INTEGER));
            int score = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "score"), INTEGER));
            return new ContestantData(uuid, number, score);
        }
    };

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
