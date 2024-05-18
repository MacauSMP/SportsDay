package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.macausmp.sportsday.util.ContestantData;

import java.util.HashMap;
import java.util.Objects;

public abstract class AbstractTrackEvent extends AbstractEvent implements ITrackEvent {
    public static final @NotNull Material FINISH_LINE = getMaterial("finish_line_block");
    private final HashMap<ContestantData, Integer> lapMap = new HashMap<>();
    private final HashMap<ContestantData, Float> record = new HashMap<>();
    private final int laps;
    private int time = 0;
    private boolean endCountdown = false;
    private BukkitTask task;

    public AbstractTrackEvent(String id) {
        super(id);
        this.laps = PLUGIN.getConfig().getInt(getID() + ".laps");
    }

    @Override
    public void setup() {
        lapMap.clear();
        record.clear();
        endCountdown = false;
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".ready_command")));
        super.setup();
        getContestants().forEach(data -> lapMap.put(data, 0));
        Bukkit.broadcast(Component.translatable("event.track.laps").args(Component.text(laps)).color(NamedTextColor.GREEN));
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
    public void end(boolean force) {
        super.end(force);
        if (force)
            return;
        Component c = Component.translatable("event.result");
        for (int i = 0; i < getLeaderboard().size();) {
            ContestantData data = getLeaderboard().get(i);
            c = c.appendNewline().append(Component.translatable("event.track.rank")
                    .args(Component.text(++i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i <= 3)
                data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @EventHandler
    public void onEvent(@NotNull PlayerMoveEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStatus() == Status.STARTED && Competitions.isContestant(p)) {
            ContestantData data = Competitions.getContestant(p.getUniqueId());
            if (getLeaderboard().contains(data) || !lapMap.containsKey(data))
                return;
            Location loc = p.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            if (loc.getBlock().getType() == FINISH_LINE) {
                lapMap.put(data, lapMap.get(data) + 1);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
                if (lapMap.get(data) < laps) {
                    p.teleport(getLocation());
                    p.setBedSpawnLocation(getLocation(), true);
                    onCompletedLap(p);
                    Bukkit.broadcast(Component.translatable("event.track.contestant.completed_lap")
                            .args(p.displayName()).color(NamedTextColor.YELLOW));
                } else {
                    record.put(data, time / 20f);
                    p.setGameMode(GameMode.SPECTATOR);
                    getLeaderboard().add(Competitions.getContestant(p.getUniqueId()));
                    onRaceFinish(p);
                    Bukkit.broadcast(Component.translatable("event.track.contestant.completed_all")
                            .args(p.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW));
                    if (getLeaderboard().size() == getContestants().size()) {
                        if (task != null && !task.isCancelled())
                            task.cancel();
                        PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.all_completed"));
                        end(false);
                        return;
                    }
                    if (getLeaderboard().size() >= 3 && !endCountdown) {
                        endCountdown = true;
                        Bukkit.broadcast(Component.translatable("event.track.end.countdown.notice")
                                .args(Component.text(PLUGIN.getConfig().getInt("event_end_countdown"))));
                        task = addRunnable(new BukkitRunnable() {
                            int i = PLUGIN.getConfig().getInt("event_end_countdown");
                            @Override
                            public void run() {
                                if (i > 0)
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown")
                                            .args(Component.text(i)).color(NamedTextColor.GREEN));
                                if (i-- == 0) {
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown_end"));
                                    end(false);
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
            if (loc.getBlock().getType() == FINISH_LINE) {
                p.teleport(getLocation());
                p.setBedSpawnLocation(getLocation(), true);
                p.sendMessage(Component.translatable("contestant.practice.finished").args(getName()));
                onCompletedLap(p);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            }
        }
    }

    @Override
    public void onDisqualification(@NotNull ContestantData contestant) {
        super.onDisqualification(contestant);
        if (getLeaderboard().size() == getContestants().size()) {
            if (task != null && !task.isCancelled())
                task.cancel();
            end(false);
        }
    }

    protected void onCompletedLap(@NotNull Player player) {}

    protected void onRaceFinish(@NotNull Player player) {}

    @Override
    public int getMaxLaps() {
        return laps;
    }

    public static @NotNull Material getMaterial(@NotNull String path) {
        return Material.valueOf(PLUGIN.getConfig().getString(path));
    }
}
