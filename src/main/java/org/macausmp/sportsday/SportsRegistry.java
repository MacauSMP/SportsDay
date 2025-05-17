package org.macausmp.sportsday;

import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.sport.SportType;
import org.macausmp.sportsday.venue.VenueType;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class SportsRegistry<T extends Keyed> implements Registry<T> {
    private static final Set<Supplier<?>> DEFAULT_ENTRIES = new HashSet<>();
    public static final SportsRegistry<SportType> SPORT_TYPE = create(() -> SportType.ATHLETICS);
    public static final SportsRegistry<VenueType<?>> VENUE_TYPE = create(() -> VenueType.VENUE);
    public static final SportsRegistry<Sport> SPORT = create(() -> Sport.ELYTRA_RACING);

    private static <T extends Keyed> @NotNull SportsRegistry<T> create(@NotNull Supplier<T> supplier) {
        SportsRegistry<T> registry = new SportsRegistry<>();
        DEFAULT_ENTRIES.add(supplier);
        return registry;
    }

    public static void init() {
        DEFAULT_ENTRIES.forEach(Supplier::get);
    }

    private final List<T> idToValue = new ArrayList<>();
    private final Map<NamespacedKey, T> keyToValue = new HashMap<>();
    private final Map<T, NamespacedKey> valueToKey = new HashMap<>();

    private SportsRegistry() {}

    public void add(@NotNull NamespacedKey key, @NotNull T value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Preconditions.checkState(!keyToValue.containsKey(key), "Adding duplicate key '%s' to registry", key);
        Preconditions.checkState(!valueToKey.containsKey(value), "Adding duplicate value '%s' to registry", value);
        idToValue.add(value);
        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    @Override
    public @Nullable T get(@NotNull NamespacedKey key) {
        return keyToValue.get(key);
    }

    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey key) {
        T value = get(key);
        Preconditions.checkArgument(value != null, "No registry entry found for key %s.", key);
        return value;
    }

    @Override
    public @Nullable NamespacedKey getKey(@NotNull T value) {
        return valueToKey.get(value);
    }

    @Override
    public @NotNull Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return idToValue.iterator();
    }

    public int size() {
        return idToValue.size();
    }
}
