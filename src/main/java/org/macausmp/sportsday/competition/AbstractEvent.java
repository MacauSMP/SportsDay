package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.customize.Musickit;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.customize.VictoryDance;
import org.macausmp.sportsday.gui.competition.event.EventGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.*;

public abstract class AbstractEvent implements IEvent {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Set<BukkitTask> EVENT_TASKS = new HashSet<>();
    private static final Map<Player, IEvent> PRACTICE = new HashMap<>();
    private final NamespacedKey key;
    private final String id;
    private final Component name;
    private final int least;
    private final Location location;
    private final World world;
    private Status status = Status.IDLE;
    private boolean pause = false;
    private long time = 0L;
    private final Collection<ContestantData> contestants = new HashSet<>();
    private final List<ContestantData> leaderboard = new ArrayList<>();
    private VictoryDance victoryDance = null;
    private Musickit mvpAnthem = null;
    public static final NamespacedKey IN_GAME = new NamespacedKey(PLUGIN, "in_game");

    public AbstractEvent(String id) {
        this.key = new NamespacedKey(PLUGIN, id);
        this.id = id;
        this.name = TextUtil.convert(Component.translatable("event.name." + id));
        this.least = PLUGIN.getConfig().getInt(id + ".least_players_required");
        this.location = Objects.requireNonNull(PLUGIN.getConfig().getLocation(id + ".location"));
        this.world = location.getWorld();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public final String getID() {
        return id;
    }

    @Override
    public final Component getName() {
        return name;
    }

    @Override
    public final int getLeastPlayersRequired() {
        return least;
    }

    @Override
    public final Location getLocation() {
        return location;
    }

    @Override
    public final World getWorld() {
        return world;
    }

    @Override
    public final boolean isEnable() {
        return PLUGIN.getConfig().getBoolean(id + ".enable");
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    /**
     * Set the current event status.
     *
     * @param status new status
     */
    protected final void setStatus(Status status) {
        this.status = status;
        EventGUI.updateGUI();
    }

    @Override
    public boolean isPaused() {
        return pause;
    }

    @Override
    public final long getLastTime() {
        return time;
    }

    @Override
    public final Collection<ContestantData> getContestants() {
        return contestants;
    }

    @Override
    public final List<ContestantData> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void setup() {
        init();
        addRunnable(new BukkitRunnable() {
            int i = PLUGIN.getConfig().getInt("ready_time");
            @Override
            public void run() {
                if (pause) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.pause"));
                    return;
                }
                if (i % 5 == 0 || i <= 5 && i > 0) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.start_countdown")
                            .arguments(Component.text(i)).color(NamedTextColor.GREEN));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"),
                            Sound.Source.MASTER, 1f, 0.5f));
                }
                if (i-- == 0) {
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        Bukkit.broadcast(Component.translatable("event.broadcast.ready")
                .arguments(name, Component.text(PLUGIN.getConfig().getInt("ready_time"))).color(NamedTextColor.GREEN));
        Bukkit.broadcast(Component.translatable("event.rule." + id));
        onSetup();
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.coming").arguments(name));
    }

    protected void init() {
        pause = false;
        time = System.currentTimeMillis();
        Bukkit.getOnlinePlayers().forEach(p -> {
            leavePractice(p);
            p.getPersistentDataContainer().set(IN_GAME, PersistentDataType.LONG, time);
        });
        PRACTICE.clear();
        setStatus(Status.COMING);
        contestants.addAll(Competitions.getOnlineContestants());
        contestants.forEach(data -> {
            Player p = data.getPlayer();
            if (!SportsDay.REFEREES.hasPlayer(p))
                p.getInventory().clear();
            p.clearActivePotionEffects();
            p.setFireTicks(0);
            p.setGameMode(GameMode.ADVENTURE);
            PlayerCustomize.suitUp(p);
            p.getInventory().setItem(4, ItemUtil.SPRAY);
            p.setRespawnLocation(location, true);
            p.teleportAsync(location);
        });
    }

    @Override
    public void start() {
        setStatus(Status.STARTED);
        onStart();
        Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.start"));
        Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"),
                Sound.Source.MASTER, 1f, 1f));
    }

    @Override
    public void end() {
        if (status == Status.ENDED)
            return;
        setStatus(Status.ENDED);
        onEnd();
        EVENT_TASKS.forEach(BukkitTask::cancel);
        Competitions.clearEventData();
        if (!leaderboard.isEmpty()) {
            addRunnable(new BukkitRunnable() {
                @Override
                public void run() {
                    OfflinePlayer mvp = leaderboard.getFirst().getOfflinePlayer();
                    if (!mvp.isOnline())
                        return;
                    Player p = Objects.requireNonNull(mvp.getPlayer());
                    victoryDance = PlayerCustomize.getVictoryDance(p);
                    if (victoryDance != null)
                        victoryDance.play(p);
                    mvpAnthem = PlayerCustomize.getMusickit(mvp.getPlayer());
                    if (mvpAnthem != null) {
                        Bukkit.getServer().playSound(Sound.sound(mvpAnthem.getKey(), Sound.Source.MASTER, 1f, 1f));
                        Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.play_mvp_anthem")
                                .arguments(Component.text(Objects.requireNonNull(mvp.getName())).color(NamedTextColor.YELLOW),
                                        mvpAnthem.getName()));
                    }
                }
            }.runTaskLater(PLUGIN, 40L));
        }
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (!leaderboard.isEmpty() && victoryDance != null)
                    victoryDance.stop(leaderboard.getFirst().getOfflinePlayer().getUniqueId());
                victoryDance = null;
                mvpAnthem = null;
                cleanup();
            }
        }.runTaskLater(PLUGIN, 200L));
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("event.broadcast.end"));
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.end").arguments(name));
    }

    protected void cleanup() {
        if (status != Status.ENDED)
            return;
        EVENT_TASKS.forEach(BukkitTask::cancel);
        EVENT_TASKS.clear();
        contestants.clear();
        leaderboard.clear();
        Competitions.setCurrentEvent(null);
        setStatus(Status.IDLE);
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.getPersistentDataContainer().remove(IN_GAME);
            p.teleportAsync(location);
            p.setGameMode(GameMode.ADVENTURE);
        });
        Competitions.getOnlineContestants().forEach(d -> {
            Player p = d.getPlayer();
            p.getInventory().clear();
            PlayerCustomize.suitUp(p);
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        });
        getWorld().getEntitiesByClass(ItemFrame.class).stream()
                .filter(e -> e.getPersistentDataContainer().has(SportsDay.GRAFFITI)).forEach(ItemFrame::remove);
    }

    @Override
    public void pause() {
        pause = true;
        Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.pause"));
    }

    @Override
    public void unpause() {
        pause = false;
        Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.unpause"));
    }

    @Override
    public void terminate() {
        if (status == Status.ENDED)
            return;
        setStatus(Status.ENDED);
        cleanup();
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("event.broadcast.terminate"));
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.terminate").arguments(name));
    }

    /**
     * Called when the event sets up.
     * @see #setup()
     */
    protected abstract void onSetup();

    /**
     * Called when the event starts.
     * @see #start()
     */
    protected abstract void onStart();

    /**
     * Called when the event ends.
     * @see #end()
     */
    protected abstract void onEnd();

    @Override
    public void onDisqualification(@NotNull ContestantData contestant) {
        contestants.remove(contestant);
        leaderboard.remove(contestant);
        Player p = contestant.getPlayer();
        if (p.isInsideVehicle())
            Objects.requireNonNull(p.getVehicle()).remove();
        p.clearActivePotionEffects();
        p.setFireTicks(0);
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(0, ItemUtil.MENU);
        p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        p.setRespawnLocation(p.getWorld().getSpawnLocation(), true);
    }

    @Override
    public void joinPractice(@NotNull Player player) {
        PRACTICE.put(player, this);
        player.clearActivePotionEffects();
        player.setFireTicks(0);
        player.getInventory().clear();
        PlayerCustomize.suitUp(player);
        player.getInventory().setItem(8, ItemUtil.LEAVE_PRACTICE);
        player.setRespawnLocation(location, true);
        player.teleportAsync(location);
        player.sendMessage(Component.translatable("contestant.practice.teleport.venue").arguments(name));
        onPractice(player);
        player.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    /**
     * Called when a player participates in practice.
     * @param player who going to practice this event
     */
    protected abstract void onPractice(@NotNull Player player);

    /**
     * Let players leave this practice.
     * @param player who leave practicing this event
     */
    public static void leavePractice(@NotNull Player player) {
        if (!PRACTICE.containsKey(player)) return;
        PRACTICE.remove(player);
        if (player.isInsideVehicle())
            Objects.requireNonNull(player.getVehicle()).remove();
        player.clearActivePotionEffects();
        player.setFireTicks(0);
        player.getInventory().clear();
        PlayerCustomize.suitUp(player);
        player.getInventory().setItem(0, ItemUtil.MENU);
        player.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        player.setRespawnLocation(player.getWorld().getSpawnLocation(), true);
        player.teleport(player.getWorld().getSpawnLocation());
    }

    /**
     * Check if player is practicing.
     * @param player who going to be checked
     * @return {@code True} if player is practicing
     */
    public static boolean inPractice(Player player) {
        return PRACTICE.containsKey(player);
    }

    /**
     * Check if player is practicing at specified event.
     * @param player who going to be checked
     * @param event the specified event
     * @return {@code True} if player is practicing at specified event
     * @param <T> the event type
     */
    public static <T extends IEvent> boolean inPractice(Player player, T event) {
        return PRACTICE.containsKey(player) && PRACTICE.get(player) == event;
    }

    /**
     * Add a {@link BukkitTask} to this event.
     * @param task {@link BukkitTask} to add
     */
    protected final BukkitTask addRunnable(BukkitTask task) {
        EVENT_TASKS.add(task);
        return task;
    }
}
