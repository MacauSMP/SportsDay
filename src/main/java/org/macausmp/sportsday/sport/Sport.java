package org.macausmp.sportsday.sport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.event.*;
import org.macausmp.sportsday.gui.setting.CombatSettingsGUI;
import org.macausmp.sportsday.gui.setting.SportSettingsGUI;
import org.macausmp.sportsday.gui.setting.TrackSportSettingsGUI;
import org.macausmp.sportsday.training.*;
import org.macausmp.sportsday.util.TextUtil;
import org.macausmp.sportsday.venue.Venue;
import org.macausmp.sportsday.venue.VenueType;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a sport
 */
public class Sport implements Keyed, ComponentLike {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final Sport ELYTRA_RACING = register("elytra_racing", SportType.AIR_SPORT, VenueType.TRACK,
            ElytraRacingEvent::new, TrackSportSettingsGUI::new, ElytraRacingHandler::new);
    public static final Sport ICE_BOAT_RACING = register("ice_boat_racing", SportType.MOTO_SPORT, VenueType.TRACK,
            IceBoatRacingEvent::new, TrackSportSettingsGUI::new, IceBoatRacingHandler::new);
    public static final Sport JAVELIN_THROW = register("javelin_throw", SportType.ATHLETICS, VenueType.VENUE,
            JavelinThrowEvent::new, SportSettingsGUI::new, JavelinThrowHandler::new);
    public static final Sport OBSTACLE_COURSE = register("obstacle_course", SportType.PARKOUR, VenueType.TRACK,
            ObstacleCourseEvent::new, TrackSportSettingsGUI::new, ObstacleCourseHandler::new);
    public static final Sport PARKOUR = register("parkour", SportType.PARKOUR, VenueType.TRACK,
            ParkourEvent::new, TrackSportSettingsGUI::new, ParkourHandler::new);
    public static final Sport SUMO = register("sumo", SportType.COMBAT, VenueType.COMBAT_VENUE,
            SumoEvent::new, CombatSettingsGUI::new, SumoHandler::new);

    /**
     * Register a sport.
     *
     * @param id id of sport
     * @param sportType type of sport
     * @param eventFactory function that create event
     * @param settingsGUIFunction settings gui of sport
     * @param trainingHandler training handler of sport
     * @return sport after registered
     */
    private static <V extends Venue> @NotNull Sport register(
            @NotNull String id,
            @NotNull SportType sportType,
            @NotNull VenueType<?> venueType,
            @NotNull EventFactory<V, ?> eventFactory,
            @NotNull Function<Sport, SportSettingsGUI> settingsGUIFunction,
            @NotNull Function<Sport, SportsTrainingHandler> trainingHandler) {
        Sport sport = new Sport(id, sportType, venueType, eventFactory, settingsGUIFunction, trainingHandler);
        SportsRegistry.SPORT.add(new NamespacedKey(PLUGIN, id), sport);
        return sport;
    }

    public static class Settings {
        public static final Setting<Boolean> ENABLE = new Setting<>("enable", Boolean.class);
        public static final Setting<Integer> LEAST_PLAYERS_REQUIRED = new Setting<>("least_players_required", Integer.class);
    }

    public static class TrackSettings extends Settings {
        public static final Setting<Integer> LAPS = new Setting<>("laps", Integer.class);
        public static final Setting<Boolean> ALL_CHECKPOINTS_REQUIRED = new Setting<>("all_checkpoints_required", Boolean.class);
        public static final Setting<String> READY_COMMAND = new Setting<>("ready_command", String.class);
        public static final Setting<String> START_COMMAND = new Setting<>("start_command", String.class);
    }

    public static class CombatSettings extends Settings {
        public static final Setting<Boolean> ENABLE_WEAPON = new Setting<>("enable_weapon", Boolean.class);
        public static final Setting<Integer> WEAPON_TIME = new Setting<>("weapon_time", Integer.class);
    }

    private final SportType sportType;
    private final VenueType<?> venueType;
    private final EventFactory<?, ?> eventFactory;
    private final Component name;
    private final Material displayItem;
    private final Map<Setting<?>, Object> settings = new HashMap<>();
    private final Function<Sport, SportSettingsGUI> settingsGUIFunction;
    private final Map<UUID, Venue> venues = new HashMap<>();
    private final SportsTrainingHandler trainingHandler;

    <V extends Venue> Sport(@NotNull String id,
                            @NotNull SportType sportType,
                            @NotNull VenueType<?> venueType,
                            @NotNull EventFactory<V, ?> eventFactory,
                            @NotNull Function<Sport, SportSettingsGUI> settingsGUIFunction,
                            @NotNull Function<Sport, SportsTrainingHandler> trainingHandler) {
        this.sportType = sportType;
        this.venueType = venueType;
        this.name = TextUtil.convert(Component.translatable("sport.name." + id));
        String item = PLUGIN.getConfig().getString(id + ".item");
        this.displayItem = item != null ? Material.getMaterial(item) : Material.YELLOW_STAINED_GLASS_PANE;
        this.eventFactory = eventFactory;
        this.settingsGUIFunction = settingsGUIFunction;
        this.trainingHandler = trainingHandler.apply(this);
        PLUGIN.getServer().getPluginManager().registerEvents(this.trainingHandler, PLUGIN);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return SportsRegistry.SPORT.getKeyOrThrow(this);
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }

    /**
     * Get the sport's type.
     *
     * @return type of sport
     */
    public SportType getSportType() {
        return sportType;
    }

    /**
     * Get the sport's display item.
     *
     * @return display item of sport
     */
    public @NotNull Material getDisplayItem() {
        return displayItem;
    }

    /**
     * Get the sport's settings from the config file.
     *
     * @param setting specified setting
     * @param <T> type of specified setting
     * @return specified setting of sport
     */
    public <T> T getSetting(@NotNull Setting<T> setting) {
        Object value = settings.get(setting);
        Class<T> clazz = setting.type();
        if (value == null) {
            settings.put(setting, PLUGIN.getConfig().getObject(getKey().getKey() + "." + setting.name(), clazz));
            value = settings.get(setting);
        }
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    /**
     * Set the sport's settings to the config file.
     *
     * @param setting specified setting
     * @param value new value of setting
     * @param <T> type of specified setting
     */
    public <T> void setSetting(@NotNull Setting<T> setting, T value) {
        PLUGIN.getConfig().set(getKey().getKey() + "." + setting.name(), value);
        PLUGIN.saveConfig();
        settings.put(setting, value);
        SportSettingsGUI.updateAll(SportSettingsGUI.class);
    }

    /**
     * Get the sport's settings gui.
     *
     * @return settings gui of sport
     */
    public @NotNull SportSettingsGUI getSettingGUI() {
        return settingsGUIFunction.apply(this);
    }

    /**
     * Get the sport's venues
     *
     * @return a list of venues of sport
     */
    public List<? extends Venue> getVenues() {
        return new ArrayList<>(venues.values());
    }

    public Venue addVenue(@Nullable String name, @NotNull Location location) {
        if (name != null && venues.values().stream().anyMatch(v -> v.getName().equals(name))) {
            return null;
        }
        Venue venue = venueType.create(name, location);
        venues.put(venue.getUUID(), venue);
        return venue;
    }

    public void removeVenue(@NotNull UUID uuid) {
        venues.remove(uuid);
    }

    @SuppressWarnings("unchecked")
    public <V extends Venue> void saveVenues(@NotNull PersistentDataContainer container) {
        ListPersistentDataType<PersistentDataContainer, V> dataType =
                (ListPersistentDataType<PersistentDataContainer, V>)
                        PersistentDataType.LIST.listTypeFrom(venueType.getVenueDataType());
        container.set(getKey(), dataType, (List<V>) getVenues());
    }

    public void loadVenues(@NotNull PersistentDataContainer container) {
        List<? extends Venue> list = container.get(getKey(),
                PersistentDataType.LIST.listTypeFrom(venueType.getVenueDataType()));
        if (list == null)
            return;
        list.forEach(v -> venues.put(v.getUUID(), v));
    }

    /**
     * Get the sport's training handler.
     *
     * @return training handler of sport
     */
    public SportsTrainingHandler getTrainingHandler() {
        return trainingHandler;
    }

    /**
     * Create an event for this sport
     *
     * @param venue venue to host the event
     * @return a new event of this sport
     */
    @SuppressWarnings("unchecked")
    public <V extends Venue, T extends SportingEvent> T createEvent(@NotNull Venue venue) {
        if (!getVenues().contains(venue))
            throw new IllegalArgumentException("Invalid venue for this sport.");
        return ((EventFactory<V, T>) eventFactory).create(this, (V) venue, null);
    }

    /**
     * Load a saved event
     *
     * @param save data of saved event
     * @return an event from saved data
     */
    @SuppressWarnings("unchecked")
    public <V extends Venue, T extends SportingEvent> T loadEvent(@NotNull PersistentDataContainer save) {
        UUID uuid = UUID.fromString(Objects.requireNonNull(save.get(new NamespacedKey(PLUGIN, "venue"), PersistentDataType.STRING)));
        return ((EventFactory<V, T>) eventFactory).create(this, (V) Objects.requireNonNull(venues.get(uuid)), save);
    }

    @FunctionalInterface
    interface EventFactory<V extends Venue, T extends SportingEvent> {
        T create(@NotNull Sport sport, @NotNull V venue, @Nullable PersistentDataContainer save);
    }
}
