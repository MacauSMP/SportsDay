package org.macausmp.sportsday.venue;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.gui.venue.CombatVenueSettingsGUI;
import org.macausmp.sportsday.gui.venue.GeneralVenueSettingsGUI;
import org.macausmp.sportsday.gui.venue.TrackSettingsGUI;
import org.macausmp.sportsday.gui.venue.VenueSettingsGUI;
import org.macausmp.sportsday.sport.Sport;

import java.util.UUID;
import java.util.function.BiFunction;

public class VenueType<T extends Venue> implements Keyed {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final VenueType<Venue> VENUE = register("venue",
            Venue::new, Venue.class, Venue.VENUE_DATA_TYPE, GeneralVenueSettingsGUI::new);
    public static final VenueType<Track> TRACK = register("track",
            Track::new, Track.class, Track.TRACK_DATA_TYPE, TrackSettingsGUI::new);
    public static final VenueType<CombatVenue> COMBAT_VENUE = register("combat_venue",
            CombatVenue::new, CombatVenue.class, CombatVenue.COMBAT_VENUE_DATA_TYPE, CombatVenueSettingsGUI::new);

    private static <T extends Venue> @NotNull VenueType<T> register(
            @NotNull String id,
            @NotNull VenueFactory<T> factory,
            @NotNull Class<T> venueClass,
            @NotNull PersistentDataType<PersistentDataContainer, T> venueDataType,
            @NotNull BiFunction<Sport, T, ? extends VenueSettingsGUI<T>> settingsGUIFunction) {
        VenueType<T> type = new VenueType<>(factory, venueClass, venueDataType, settingsGUIFunction);
        SportsRegistry.VENUE_TYPE.add(new NamespacedKey(PLUGIN, id), type);
        return type;
    }

    private final VenueFactory<T> factory;
    private final Class<T> venueClass;
    private final PersistentDataType<PersistentDataContainer, T> venueDataType;
    private final BiFunction<Sport, T, ? extends VenueSettingsGUI<T>> settingsGUIFunction;

    private VenueType(@NotNull VenueFactory<T> factory,
                      @NotNull Class<T> venueClass,
                      @NotNull PersistentDataType<PersistentDataContainer, T> venueDataType,
                      @NotNull BiFunction<Sport, T, ? extends VenueSettingsGUI<T>> settingsGUIFunction) {
        this.factory = factory;
        this.venueClass = venueClass;
        this.venueDataType = venueDataType;
        this.settingsGUIFunction = settingsGUIFunction;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return SportsRegistry.VENUE_TYPE.getKeyOrThrow(this);
    }

    public PersistentDataType<PersistentDataContainer, T> getVenueDataType() {
        return venueDataType;
    }

    public VenueSettingsGUI<T> getSettingsGUI(@NotNull Sport sport, @NotNull Venue venue) {
        return settingsGUIFunction.apply(sport, cast(venue));
    }

    public T create(@Nullable String name, @NotNull Location location) {
        UUID uuid = UUID.randomUUID();
        return factory.create(this, uuid, name != null ? name : "New venue " + uuid, location);
    }

    public T cast(@NotNull Venue venue) {
        return venue.getType() == this ? venueClass.cast(venue) : null;
    }

    @FunctionalInterface
    interface VenueFactory<T extends Venue> {
        T create(@NotNull VenueType<T> type, @NotNull UUID uuid, @NotNull String name, @NotNull Location location);
    }
}
