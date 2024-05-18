package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.competition.CompetitionConsoleGUI;
import org.macausmp.sportsday.gui.competition.ContestantsListGUI;
import org.macausmp.sportsday.util.ContestantData;
import org.macausmp.sportsday.util.PlayerHolder;

import java.util.*;
import java.util.stream.Collectors;

public final class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final FileConfiguration CONTESTANTS_CONFIG = PLUGIN.getConfigManager().getContestantsConfig();
    public static final Map<String, IEvent> EVENTS = new LinkedHashMap<>();
    public static final IEvent ELYTRA_RACING = register(new ElytraRacing());
    public static final IEvent ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final IEvent JAVELIN_THROW = register(new JavelinThrow());
    public static final IEvent OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final IEvent PARKOUR = register(new Parkour());
    public static final IEvent SUMO = register(new Sumo());
    private static IEvent CURRENT_EVENT;
    private static final Map<UUID, ContestantData> CONTESTANTS = new HashMap<>();
    private static final Set<Integer> REGISTERED_NUMBER_LIST = new HashSet<>();
    private static int NUMBER = 1;

    /**
     * Register competition event.
     * @param competition competition event to register
     * @return competition event after registered
     */
    private static <T extends IEvent> @NotNull T register(T competition) {
        EVENTS.put(competition.getID(), competition);
        return competition;
    }

    /**
     * Save contestants data into the contestants.yml file, in the plugin/SportsDay/ folder.
     */
    public static void save() {
        CONTESTANTS.forEach((uuid, data) -> {
            CONTESTANTS_CONFIG.set(uuid + ".name", data.getName());
            CONTESTANTS_CONFIG.set(uuid + ".number", data.getNumber());
            CONTESTANTS_CONFIG.set(uuid + ".score", data.getScore());
        });
        PLUGIN.getConfigManager().saveContestsConfig();
    }

    /**
     * Load contestants data from the contestants.yml file, in the plugin/SportsDay/ folder.
     * <p>Note: This will overwrite the contestants current data, with the state from the saved yml file.</p>
     */
    public static void load() {
        CONTESTANTS.clear();
        Set<String> keys = CONTESTANTS_CONFIG.getKeys(false);
        for (String key : keys) {
            UUID uuid = UUID.fromString(key);
            int number = CONTESTANTS_CONFIG.getInt(key + ".number");
            int score = CONTESTANTS_CONFIG.getInt((key + ".score"));
            CONTESTANTS.put(uuid, new ContestantData(uuid, number, score));
        }
    }

    /**
     * Start a competition.
     * @param sender who host the competition
     * @param id competition id
     * @return {@code True} if competition successfully started
     */
    public static boolean start(CommandSender sender, String id) {
        if (getCurrentEvent() != null && getCurrentEvent().getStatus() != Status.ENDED) {
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
                    .args(Component.text(event.getLeastPlayersRequired())).color(NamedTextColor.RED));
            return false;
        }
        sender.sendMessage(Component.translatable("command.competition.start.success").color(NamedTextColor.GREEN));
        setCurrentEvent(event);
        event.setup();
        return true;
    }

    /**
     * Force end current competition.
     * @param sender who end the competition
     * @return {@code True} if competition successfully end
     */
    public static boolean forceEnd(CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.end.failed").color(NamedTextColor.RED));
            return false;
        }
        getCurrentEvent().end(true);
        sender.sendMessage(Component.translatable("command.competition.end.success").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Get the current event.
     * @return current event
     */
    public static IEvent getCurrentEvent() {
        return CURRENT_EVENT;
    }

    /**
     * Set the current event.
     * @param event new event
     */
    public static void setCurrentEvent(IEvent event) {
        CURRENT_EVENT = event;
    }

    /**
     * Add a player to contestants list.
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
        player.sendMessage(Component.translatable("command.competition.register.success.self")
                .args(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.CONTESTANTS.addPlayer(player);
        return true;
    }

    /**
     * Remove a specific player from contestants list.
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
                    .sendMessage(Component.translatable("command.competition.unregister.success.self"));
        }
        CONTESTANTS_CONFIG.set(data.getUUID().toString(), null);
        REGISTERED_NUMBER_LIST.remove(data.getNumber());
        CONTESTANTS.remove(uuid);
        CompetitionConsoleGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        SportsDay.AUDIENCES.addPlayer(player);
        return true;
    }

    /**
     * Generate unoccupied contestant numbers.
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
     * @return a view of registered contestants
     */
    public static @NotNull Collection<ContestantData> getContestants() {
        return Map.copyOf(CONTESTANTS).values();
    }

    /**
     * Gets a view of all currently logged in registered contestants.
     * @return a view of currently online registered contestants
     */
    public static @NotNull Collection<ContestantData> getOnlineContestants() {
        return CONTESTANTS.values().stream().filter(PlayerHolder::isOnline).collect(Collectors.toSet());
    }

    /**
     * Checks if this player is in contestants list.
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
        if (data != null)
            return data;
        throw new IllegalArgumentException("Contestants data list does not contain this data");
    }
}
