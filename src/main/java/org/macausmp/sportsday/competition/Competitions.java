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
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.CompetitorListGUI;
import org.macausmp.sportsday.util.CompetitorData;

import java.util.*;

public final class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final FileConfiguration COMPETITOR_CONFIG = PLUGIN.getConfigManager().getCompetitorConfig();
    public static final Set<IEvent> EVENTS = new LinkedHashSet<>();
    public static final IEvent ELYTRA_RACING = register(new ElytraRacing());
    public static final IEvent ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final IEvent JAVELIN_THROW = register(new JavelinThrow());
    public static final IEvent OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final IEvent PARKOUR = register(new Parkour());
    public static final IEvent SUMO = register(new Sumo());
    private static IEvent CURRENT_EVENT;
    private static final Set<CompetitorData> COMPETITORS = new HashSet<>();
    private static final Set<Integer> REGISTERED_NUMBER_LIST = new HashSet<>();
    private static int NUMBER = 1;

    /**
     * Register competition event
     * @param competition Competition event to register
     * @return Competition event after registered
     */
    private static <T extends IEvent> T register(T competition) {
        EVENTS.add(competition);
        return competition;
    }

    public static void load() {
        COMPETITORS.clear();
        Set<String> keys = COMPETITOR_CONFIG.getKeys(false);
        for (String s : keys) {
            UUID uuid = UUID.fromString(s);
            int number = COMPETITOR_CONFIG.getInt(s + ".number");
            int score = COMPETITOR_CONFIG.getInt((s + ".score"));
            COMPETITORS.add(new CompetitorData(uuid, number, score));
        }
    }

    public static void save() {
        for (CompetitorData data : COMPETITORS) {
            COMPETITOR_CONFIG.set(data.getUUID() + ".name", data.getName());
            COMPETITOR_CONFIG.set(data.getUUID() + ".number", data.getNumber());
            COMPETITOR_CONFIG.set(data.getUUID() + ".score", data.getScore());
        }
        PLUGIN.getConfigManager().saveConfig();
    }

    /**
     * Start a competition
     * @param sender Who host the competition
     * @param id Competition id
     * @return True if competition successfully started
     */
    public static boolean start(CommandSender sender, String id) {
        if (getCurrentEvent() != null && getCurrentEvent().getStatus() != Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return false;
        }
        for (IEvent event : EVENTS) {
            if (event.getID().equals(id)) {
                if (!event.isEnable()) {
                    sender.sendMessage(Component.translatable("command.competition.disabled").color(NamedTextColor.RED));
                    return false;
                }
                if (getOnlineCompetitors().size() < event.getLeastPlayersRequired()) {
                    sender.sendMessage(Component.translatable("command.competition.not_enough_player_required").args(Component.text(event.getLeastPlayersRequired())).color(NamedTextColor.RED));
                    return false;
                }
                sender.sendMessage(Component.translatable("command.competition.start.success").color(NamedTextColor.GREEN));
                setCurrentEvent(event);
                event.setup();
                return true;
            }
        }
        sender.sendMessage(Component.translatable("event.name.unknown").color(NamedTextColor.RED));
        return false;
    }

    /**
     * Force end current competition
     * @param sender who end the competition
     */
    public static void forceEnd(CommandSender sender) {
        if (getCurrentEvent() != null && getCurrentEvent().getStatus() != Status.ENDED) {
            getCurrentEvent().end(true);
            sender.sendMessage(Component.translatable("command.competition.end.success").color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.translatable("command.competition.end.failed").color(NamedTextColor.RED));
        }
    }

    /**
     * Get the current event
     * @return Current event
     */
    public static IEvent getCurrentEvent() {
        return CURRENT_EVENT;
    }

    /**
     * Set the current event
     * @param event New event
     */
    public static void setCurrentEvent(IEvent event) {
        CURRENT_EVENT = event;
    }

    /**
     * Add competitor to competitor list
     * @param competitor Competitor to add to the competitor list
     * @param number Competitor number
     * @return True if competitor successfully added to the competitor list
     */
    public static boolean join(@NotNull Player competitor, int number) {
        for (CompetitorData data : COMPETITORS) {
            if (data.getNumber() == number) return false;
        }
        COMPETITORS.add(new CompetitorData(competitor.getUniqueId(), number));
        CompetitionInfoGUI.updateGUI();
        CompetitorListGUI.updateGUI();
        competitor.sendMessage(Component.translatable("command.competition.register.success.self").args(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.COMPETITOR.addPlayer(competitor);
        return true;
    }

    /**
     * Remove competitor from competitor list
     * @param competitor Competitor to remove from the competitor list
     * @return True if competitor successfully removed from the competitor list
     */
    public static boolean leave(OfflinePlayer competitor) {
        if (containPlayer(competitor)) {
            for (CompetitorData data : COMPETITORS) {
                if (data.getUUID().equals(competitor.getUniqueId())) {
                    COMPETITOR_CONFIG.set(data.getUUID().toString(), null);
                    REGISTERED_NUMBER_LIST.remove(data.getNumber());
                    if (getCurrentEvent() != null) getCurrentEvent().getCompetitors().remove(data);
                    COMPETITORS.remove(data);
                    CompetitionInfoGUI.updateGUI();
                    CompetitorListGUI.updateGUI();
                    if (competitor.isOnline()) Objects.requireNonNull(competitor.getPlayer()).sendMessage(Component.translatable("command.competition.unregister.success.self"));
                    SportsDay.AUDIENCE.addPlayer(competitor);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate unoccupied competitor numbers
     * @return Unoccupied competitor number
     */
    public static int genNumber() {
        COMPETITORS.forEach(data -> REGISTERED_NUMBER_LIST.add(data.getNumber()));
        while (REGISTERED_NUMBER_LIST.contains(NUMBER)) {
            NUMBER++;
        }
        REGISTERED_NUMBER_LIST.add(NUMBER);
        return NUMBER;
    }

    /**
     * Gets a view of all registered competitors
     * @return a view of registered competitors
     */
    public static Collection<CompetitorData> getCompetitors() {
        return COMPETITORS;
    }

    /**
     * Gets a view of all currently logged in registered competitors
     * @return a view of currently online registered competitors
     */
    public static @NotNull Collection<CompetitorData> getOnlineCompetitors() {
        return COMPETITORS.stream().filter(d -> d.getOfflinePlayer().isOnline()).toList();
    }

    /**
     * Return true if the player is in competition player list
     * @param player player who presence in competition player list is to be tested
     * @return True if the player is in competition player list
     */
    public static boolean containPlayer(OfflinePlayer player) {
        for (CompetitorData data : COMPETITORS) {
            if (data.getUUID().equals(player.getUniqueId())) return true;
        }
        return false;
    }

    /**
     * Get competitor data by uuid
     *
     * <p>Plugins should check that {@link #containPlayer(OfflinePlayer)} returns <code>true</code> before calling this method.</p>
     *
     * @param uuid Player uuid
     * @return Competitor data
     */
    public static @NotNull CompetitorData getCompetitor(UUID uuid) {
        for (CompetitorData data : COMPETITORS) {
            if (data.getUUID().equals(uuid)) return data;
        }
        throw new IllegalArgumentException("Competitor data list does not contain this data");
    }
}
