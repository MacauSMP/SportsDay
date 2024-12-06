package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.competition.event.TrackEventGUI;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractTrackEvent extends AbstractEvent implements ITrackEvent {
    private static final Set<UUID> SPAWNPOINT_SET = new HashSet<>();
    public static final @NotNull Material CHECKPOINT = getMaterial("checkpoint_block");
    public static final @NotNull Material DEATH = getMaterial("death_block");
    public static final @NotNull Material FINISH_LINE = getMaterial("finish_line_block");
    protected final Predicate<Player> predicate = p -> Competitions.getCurrentEvent() == this
            && Competitions.isContestant(p) && !getLeaderboard().contains(Competitions.getContestant(p.getUniqueId()))
            || inPractice(p, this);
    private final HashMap<ContestantData, Integer> lapMap = new HashMap<>();
    private final HashMap<ContestantData, Float> record = new HashMap<>();
    private int laps;
    private int time = 0;
    private boolean endCountdown = false;
    private BukkitTask task;

    public AbstractTrackEvent(String id, Material icon) {
        super(id, icon);
        this.laps = PLUGIN.getConfig().getInt(getID() + ".laps");
    }

    @Override
    public void setup() {
        lapMap.clear();
        record.clear();
        laps = PLUGIN.getConfig().getInt(getID() + ".laps");
        endCountdown = false;
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".ready_command")));
        super.setup();
        getContestants().forEach(data -> lapMap.put(data, 0));
        Bukkit.broadcast(Component.translatable("event.track.laps").arguments(Component.text(laps)).color(NamedTextColor.GREEN));
    }

    @Override
    public void start() {
        super.start();
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".start_command")));
        time = 0;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (getStatus() != Status.STARTED) {
                    cancel();
                    return;
                }
                time++;
            }
        }.runTaskTimer(PLUGIN, 0L, 1L));
    }

    @Override
    public void end() {
        super.end();
        TranslatableComponent.Builder builder = Component.translatable("event.result").toBuilder();
        for (int i = 0; i < getLeaderboard().size();) {
            ContestantData data = getLeaderboard().get(i);
            builder.appendNewline().append(Component.translatable("event.track.rank")
                    .arguments(Component.text(++i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i <= 3)
                data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(builder.build());
    }

    @EventHandler
    public void onEvent(@NotNull PlayerMoveEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStatus() == Status.STARTED && Competitions.isContestant(p)) {
            ContestantData data = Competitions.getContestant(p.getUniqueId());
            if (getLeaderboard().contains(data) || !lapMap.containsKey(data))
                return;
            Location loc = e.getTo().clone();
            loc.setY(loc.getY() - 0.5f);
            spawnpoint(p, loc);
            if (loc.getBlock().getType() == DEATH)
                p.setHealth(0);
            if (loc.getBlock().getType() == FINISH_LINE) {
                lapMap.put(data, lapMap.get(data) + 1);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
                if (lapMap.get(data) < laps) {
                    p.teleportAsync(getLocation());
                    p.setRespawnLocation(getLocation(), true);
                    onCompletedLap(p);
                    Bukkit.broadcast(Component.translatable("event.track.contestant.completed_lap")
                            .arguments(p.displayName()).color(NamedTextColor.YELLOW));
                    TrackEventGUI.updateGUI();
                } else {
                    record.put(data, time / 20f);
                    p.setGameMode(GameMode.SPECTATOR);
                    getLeaderboard().add(Competitions.getContestant(p.getUniqueId()));
                    onRaceFinish(p);
                    Bukkit.broadcast(Component.translatable("event.track.contestant.completed_all")
                            .arguments(p.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW));
                    TrackEventGUI.updateGUI();
                    if (getLeaderboard().size() == getContestants().size()) {
                        if (task != null && !task.isCancelled())
                            task.cancel();
                        PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.all_completed"));
                        end();
                        return;
                    }
                    if (getLeaderboard().size() >= 3 && !endCountdown) {
                        endCountdown = true;
                        Bukkit.broadcast(Component.translatable("event.track.end.countdown.notice")
                                .arguments(Component.text(PLUGIN.getConfig().getInt("event_end_countdown"))));
                        task = addRunnable(new BukkitRunnable() {
                            int i = PLUGIN.getConfig().getInt("event_end_countdown");
                            @Override
                            public void run() {
                                if (i > 0)
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown")
                                            .arguments(Component.text(i)).color(NamedTextColor.GREEN));
                                if (i-- == 0) {
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown_end"));
                                    end();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(PLUGIN, 0L, 20L));
                    }
                }
            }
        } else if (inPractice(p, this)) {
            Location loc = p.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            spawnpoint(p, loc);
            if (loc.getBlock().getType() == DEATH)
                p.setHealth(0);
            if (loc.getBlock().getType() == FINISH_LINE) {
                p.teleportAsync(getLocation());
                p.setRespawnLocation(getLocation(), true);
                p.sendMessage(Component.translatable("contestant.practice.finished").arguments(getName()));
                onCompletedLap(p);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            }
        }
    }

    private void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        UUID uuid = player.getUniqueId();
        if (loc.getBlock().getType() == CHECKPOINT && !SPAWNPOINT_SET.contains(uuid)) {
            SPAWNPOINT_SET.add(uuid);
            player.setRespawnLocation(player.getLocation(), true);
            player.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            player.sendActionBar(Component.text("Checkpoint").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
        }
        if (loc.getBlock().getType() != CHECKPOINT)
            SPAWNPOINT_SET.remove(uuid);
    }

    @Override
    public void onDisqualification(@NotNull ContestantData contestant) {
        super.onDisqualification(contestant);
        if (getLeaderboard().size() == getContestants().size()) {
            if (task != null && !task.isCancelled())
                task.cancel();
            end();
        }
    }

    protected void onCompletedLap(@NotNull Player player) {}

    protected void onRaceFinish(@NotNull Player player) {}

    @Override
    public int getMaxLaps() {
        return laps;
    }

    @Override
    public float getRecord(ContestantData data) {
        return Optional.ofNullable(record.get(data)).orElse(-1f);
    }

    public static @NotNull Material getMaterial(@NotNull String path) {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString(path))));
    }
}
