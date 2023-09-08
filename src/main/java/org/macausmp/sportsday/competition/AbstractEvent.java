package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.util.*;

import java.util.*;

public abstract class AbstractEvent implements IEvent {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final List<BukkitTask> EVENT_TASKS = new ArrayList<>();
    private final String id;
    private final Component name;
    private final int least;
    private final Location location;
    private final World world;
    private Stage stage = Stage.IDLE;
    private final List<CompetitorData> competitors = new ArrayList<>();
    private static final Map<Player, IEvent> PRACTICE = new HashMap<>();

    public AbstractEvent(String id) {
        this.id = id;
        this.name = TextUtil.convert(Component.translatable("event.name." + id));
        this.least = PLUGIN.getConfig().getInt(id + ".least_players_required");
        this.location = Objects.requireNonNull(PLUGIN.getConfig().getLocation(id + ".location"));
        this.world = location.getWorld();
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
    public void setup() {
        PRACTICE.clear();
        EVENT_TASKS.forEach(BukkitTask::cancel);
        EVENT_TASKS.clear();
        competitors.clear();
        getLeaderboard().clear();
        setStage(Stage.COMING);
        competitors.addAll(Competitions.getOnlineCompetitors());
        competitors.forEach(data -> {
            Player p = data.getPlayer();
            p.setBedSpawnLocation(location, true);
            if (!SportsDay.REFEREE.hasPlayer(p)) p.getInventory().clear();
            p.clearActivePotionEffects();
            p.setFireTicks(0);
            p.setHealth(20);
            p.teleport(location);
            p.setGameMode(GameMode.ADVENTURE);
            PlayerCustomize.suitUp(p);
            p.getInventory().setItem(4, ItemUtil.SPRAY);
        });
        addRunnable(new BukkitRunnable() {
            int i = PLUGIN.getConfig().getInt("ready_time");
            @Override
            public void run() {
                if (i % 5 == 0 || (i <= 5 && i > 0)) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.start.countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER,  1f, 0.5f));
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        Bukkit.broadcast(Component.translatable("event.ready.broadcast").args(name, Component.text(PLUGIN.getConfig().getInt("ready_time"))).color(NamedTextColor.GREEN));
        Bukkit.broadcast(Component.translatable("event.rule." + id));
        onSetup();
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.coming").args(name));
    }

    @Override
    public void start() {
        setStage(Stage.STARTED);
        onStart();
        Bukkit.getServer().sendActionBar(Component.translatable("event.start.broadcast"));
        Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    @Override
    public void end(boolean force) {
        setStage(Stage.ENDED);
        onEnd(force);
        EVENT_TASKS.forEach(BukkitTask::cancel);
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (stage == Stage.ENDED) {
                    Competitions.setCurrentEvent(null);
                    setStage(Stage.IDLE);
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.teleport(location);
                        p.setGameMode(GameMode.ADVENTURE);
                    });
                    Competitions.getOnlineCompetitors().forEach(d -> {
                        d.getPlayer().getInventory().clear();
                        PlayerCustomize.suitUp(d.getPlayer());
                        d.getPlayer().getInventory().setItem(3, ItemUtil.MENU);
                        d.getPlayer().getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                    });
                    getWorld().getEntitiesByClass(ItemFrame.class).forEach(e -> {
                        if (e.getPersistentDataContainer().has(CompetitionListener.GRAFFITI)) e.remove();
                    });
                }
            }
        }.runTaskLater(PLUGIN, 100L));
        if (!force) {
            OfflinePlayer mvp = getLeaderboard().get(0).getOfflinePlayer();
            CustomizeMusickit musickit = PlayerCustomize.getMusickit(mvp);
            if (musickit != null) {
                Bukkit.getServer().playSound(Sound.sound(musickit.getKey(), Sound.Source.MASTER, 1f, 1f));
                Bukkit.getServer().sendActionBar(Component.translatable("broadcast.play_mvp_anthem").args(Component.text(Objects.requireNonNull(mvp.getName())).color(NamedTextColor.YELLOW), musickit.getName()));
            }
        }
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("event.end.broadcast"));
        PLUGIN.getComponentLogger().info(Component.translatable(force ? "console.competition.force_end" : "console.competition.end").args(name));
    }

    /**
     * Called when the event sets up
     * @see IEvent#setup()
     */
    protected abstract void onSetup();

    /**
     * Called when the event starts
     * @see IEvent#start()
     */
    protected abstract void onStart();

    /**
     * Called when the event ends
     * @see IEvent#end(boolean)
     */
    protected abstract void onEnd(boolean force);

    @Override
    public final Stage getStage() {
        return stage;
    }

    /**
     * Set the current event stage
     * @param stage new stage
     */
    protected void setStage(Stage stage) {
        this.stage = stage;
        CompetitionInfoGUI.updateGUI();
    }

    @Override
    public void practice(@NotNull Player p) {
        PRACTICE.put(p, this);
        p.teleport(location);
        p.setBedSpawnLocation(location, true);
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(8, ItemUtil.QUIT_PRACTICE);
        p.sendMessage(Component.translatable("competitor.practice.teleport.venue").args(name));
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        onPractice(p);
    }

    protected abstract void onPractice(Player p);

    public static void quitPractice(Player p) {
        PRACTICE.remove(p);
        if (p.isInsideVehicle()) Objects.requireNonNull(p.getVehicle()).remove();
        p.teleport(p.getWorld().getSpawnLocation());
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(3, ItemUtil.MENU);
        p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
    }

    public static <T extends IEvent> boolean inPractice(Player p, T event) {
        return PRACTICE.containsKey(p) && PRACTICE.get(p).equals(event);
    }

    public static boolean inPractice(Player p) {
        return PRACTICE.containsKey(p);
    }

    public static <T extends IEvent> T getPracticeEvent(Player p) {
        //noinspection unchecked
        return (T) PRACTICE.get(p);
    }

    /**
     * Get the list of {@link CompetitorData} of current event
     * @return list of {@link CompetitorData} of current event
     */
    public final List<CompetitorData> getCompetitors() {
        return competitors;
    }

    /**
     * Add a {@link BukkitTask} to this event
     * @param task {@link BukkitTask} to add
     */
    protected BukkitTask addRunnable(BukkitTask task) {
        EVENT_TASKS.add(task);
        return task;
    }
}
