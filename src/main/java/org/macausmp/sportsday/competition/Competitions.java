package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.competition.CompetitionConsoleGUI;
import org.macausmp.sportsday.gui.competition.ContestantsListGUI;
import org.macausmp.sportsday.util.FileStorage;
import org.macausmp.sportsday.util.PlayerHolder;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final FileStorage CONTESTANTS_DATA = new FileStorage(new File("contestants.dat"));
    private static final FileStorage EVENT_DATA = new FileStorage(new File("event.dat"));
    private static final Map<UUID, ContestantData> CONTESTANTS = new HashMap<>();
    private static final Set<Integer> REGISTERED_NUMBER_LIST = new HashSet<>();
    private static int NUMBER = 1;
    public static final Map<String, IEvent> EVENTS = new LinkedHashMap<>();
    public static final IEvent ELYTRA_RACING = register(new ElytraRacing());
    public static final IEvent ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final IEvent JAVELIN_THROW = register(new JavelinThrow());
    public static final IEvent OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final IEvent PARKOUR = register(new Parkour());
    public static final IEvent SUMO = register(new Sumo());
    private static IEvent CURRENT_EVENT;

    /**
     * Register competition event.
     *
     * @param competition competition event to register
     * @return competition event after registered
     */
    private static <T extends IEvent> @NotNull T register(T competition) {
        EVENTS.put(competition.getID(), competition);
        return competition;
    }

    /**
     * Load contestants data from the contestants.dat file, in the server folder.
     *
     * <p>Note: This will overwrite the contestants current data, with the state from the saved dat file.</p>
     */
    public static void load() {
        CONTESTANTS.clear();
        CONTESTANTS_DATA.read();
        PersistentDataContainer pdc = CONTESTANTS_DATA.getPersistentDataContainer();
        for (NamespacedKey key : pdc.getKeys()) {
            ContestantData data = Objects.requireNonNull(pdc.get(key, ContestantData.CONTESTANT_DATA));
            CONTESTANTS.put(data.getUUID(), data);
        }
    }

    /**
     * Save contestants data into the contestants.dat file, in the server folder.
     */
    public static void save() {
        CONTESTANTS.forEach((uuid, data) -> CONTESTANTS_DATA.getPersistentDataContainer()
                .set(new NamespacedKey(PLUGIN, uuid.toString()), ContestantData.CONTESTANT_DATA, data));
        CONTESTANTS_DATA.write();
    }

    /**
     * Load event data from the event.dat file and start the event.
     */
    public static void loadEventData(CommandSender sender) {
        if (getCurrentEvent() != null) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return;
        }
        EVENT_DATA.read();
        PersistentDataContainer pdc = EVENT_DATA.getPersistentDataContainer();
        String id = pdc.get(new NamespacedKey(PLUGIN, "event_id"), PersistentDataType.STRING);
        if (id == null) {
            sender.sendMessage(Component.translatable("command.competition.load.failed")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!Competitions.EVENTS.containsKey(id)) {
            sender.sendMessage(Component.translatable("command.competition.load_unknown")
                    .color(NamedTextColor.RED));
            return;
        }
        IEvent event = Competitions.EVENTS.get(id);
        if (!(event instanceof Savable savable)) {
            sender.sendMessage(Component.translatable("command.competition.not_savable")
                    .color(NamedTextColor.RED));
            return;
        }
        Competitions.setCurrentEvent(event);
        savable.load(pdc);
        sender.sendMessage(Component.translatable("command.competition.load.success")
                .color(NamedTextColor.GREEN));
    }

    /**
     * Save event data into the event.dat file.
     */
    public static void saveEventData(CommandSender sender) {
        IEvent event = Competitions.getCurrentEvent();
        if (event == null || event.getStatus() != Status.STARTED) {
            sender.sendMessage(Component.translatable("command.competition.invalid_status")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!(event instanceof Savable savable)) {
            sender.sendMessage(Component.translatable("command.competition.not_savable")
                    .color(NamedTextColor.RED));
            return;
        }
        PersistentDataContainer pdc = EVENT_DATA.getPersistentDataContainer();
        savable.save(pdc);
        pdc.set(new NamespacedKey(PLUGIN, "event_id"), PersistentDataType.STRING, event.getID());
        EVENT_DATA.write();
        sender.sendMessage(Component.translatable("command.competition.save.success")
                .color(NamedTextColor.GREEN));
    }

    /**
     * Clear event data and save into the event.dat file.
     */
    public static void clearEventData() {
        EVENT_DATA.clear();
    }

    /**
     * Start a competition.
     *
     * @param sender who host the competition
     * @param id competition id
     * @return {@code True} if competition successfully started
     */
    public static boolean start(@NotNull CommandSender sender, String id) {
        if (getCurrentEvent() != null) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return false;
        }
        IEvent event = EVENTS.get(id);
        if (event == null) {
            sender.sendMessage(Component.translatable("event.name.unknown").color(NamedTextColor.RED));
            return false;
        }
        if (!event.isEnable()) {
            sender.sendMessage(Component.translatable("command.competition.disabled").color(NamedTextColor.RED));
            return false;
        }
        if (getOnlineContestants().size() < event.getLeastPlayersRequired()) {
            sender.sendMessage(Component.translatable("command.competition.not_enough_player_required")
                    .arguments(Component.text(event.getLeastPlayersRequired())).color(NamedTextColor.RED));
            return false;
        }
        sender.sendMessage(Component.translatable("command.competition.start.success").color(NamedTextColor.GREEN));
        setCurrentEvent(event);
        event.setup();
        return true;
    }

    /**
     * Force end current competition.
     *
     * @param sender who end the competition
     * @return {@code True} if competition successfully ended
     */
    public static boolean forceEnd(CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return false;
        }
        getCurrentEvent().end(true);
        sender.sendMessage(Component.translatable("command.competition.end.success").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Pause current competition.
     *
     * @param sender who pause the competition
     */
    public static boolean pause(CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return false;
        }
        if (getCurrentEvent().isPaused() || getCurrentEvent() instanceof ITrackEvent && getCurrentEvent().getStatus() == Status.STARTED) {
            sender.sendMessage(Component.translatable("command.competition.pause.failed").color(NamedTextColor.RED));
            return false;
        }
        getCurrentEvent().pause();
        sender.sendMessage(Component.translatable("command.competition.pause.success").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Unpause current competition.
     *
     * @param sender who unpause the competition
     */
    public static boolean unpause(CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return false;
        }
        if (!getCurrentEvent().isPaused()) {
            sender.sendMessage(Component.translatable("command.competition.unpause.failed").color(NamedTextColor.RED));
            return false;
        }
        getCurrentEvent().unpause();
        sender.sendMessage(Component.translatable("command.competition.unpause.success").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Get the current event.
     *
     * @return current event
     */
    public static IEvent getCurrentEvent() {
        return CURRENT_EVENT;
    }

    /**
     * Set the current event.
     *
     * @param event new event
     */
    public static void setCurrentEvent(IEvent event) {
        CURRENT_EVENT = event;
    }

    /**
     * Add a player to contestants list.
     *
     * @param player player to add to the contestants list
     * @param number player's entry number
     * @return {@code True} if player successfully added to the contestants list
     */
    public static boolean join(@NotNull Player player, int number) {
        UUID uuid = player.getUniqueId();
        if (CONTESTANTS.containsKey(uuid))
            return false;
        CONTESTANTS.put(uuid, new ContestantData(uuid, number));
        CompetitionConsoleGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        player.sendMessage(Component.translatable("competition.register.success")
                .arguments(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.CONTESTANTS.addPlayer(player);
        return true;
    }

    /**
     * Remove a specific player from contestants list.
     *
     * @param player player to remove from the contestants list
     * @return {@code True} if player successfully removed from the contestants list
     */
    public static boolean leave(OfflinePlayer player) {
        if (!isContestant(player))
            return false;
        UUID uuid = player.getUniqueId();
        ContestantData data = getContestant(uuid);
        data.remove();
        if (player.isOnline()) {
            if (getCurrentEvent() != null)
                getCurrentEvent().onDisqualification(data);
            Objects.requireNonNull(player.getPlayer())
                    .sendMessage(Component.translatable("competition.unregister.success"));
        }
        REGISTERED_NUMBER_LIST.remove(data.getNumber());
        CONTESTANTS.remove(uuid);
        CompetitionConsoleGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        SportsDay.AUDIENCES.addPlayer(player);
        return true;
    }

    /**
     * Generate unoccupied contestant numbers.
     *
     * @return unoccupied contestant number
     */
    public static int genNumber() {
        CONTESTANTS.values().forEach(data -> REGISTERED_NUMBER_LIST.add(data.getNumber()));
        while (REGISTERED_NUMBER_LIST.contains(NUMBER))
            NUMBER++;
        REGISTERED_NUMBER_LIST.add(NUMBER);
        return NUMBER;
    }

    /**
     * Gets a view of all registered contestants.
     *
     * @return a view of registered contestants
     */
    public static @NotNull @Unmodifiable Collection<ContestantData> getContestants() {
        return List.copyOf(CONTESTANTS.values());
    }

    /**
     * Gets a view of all currently logged in registered contestants.
     *
     * @return a view of currently online registered contestants
     */
    public static @NotNull Collection<ContestantData> getOnlineContestants() {
        return CONTESTANTS.values().stream().filter(PlayerHolder::isOnline).collect(Collectors.toSet());
    }

    /**
     * Checks if this player is in contestants list.
     *
     * @param player specified player
     * @return {@code True} if the player is in contestants list
     */
    public static boolean isContestant(@NotNull OfflinePlayer player) {
        return CONTESTANTS.containsKey(player.getUniqueId());
    }

    /**
     * Get {@link ContestantData} by uuid.
     *
     * <p>Plugins should check that {@link #isContestant(OfflinePlayer)} returns {@code True} before calling this method.</p>
     *
     * @param uuid player uuid
     * @return {@link ContestantData} of uuid
     */
    public static @NotNull ContestantData getContestant(@NotNull UUID uuid) {
        ContestantData data = CONTESTANTS.get(uuid);
        if (data == null)
            throw new IllegalArgumentException("Contestants data collection does not contain this data");
        return data;
    }
}
