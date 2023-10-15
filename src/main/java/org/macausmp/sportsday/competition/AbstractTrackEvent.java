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
import org.macausmp.sportsday.util.CompetitorData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTrackEvent extends AbstractEvent implements ITrackEvent {
    public static final Material FINISH_LINE = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("finish_line_block")));
    private final List<CompetitorData> leaderboard = new ArrayList<>();
    private final HashMap<CompetitorData, Integer> lapMap = new HashMap<>();
    private final HashMap<CompetitorData, Float> record = new HashMap<>();
    private final int laps;
    private float time = 0f;
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
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".ready_command")));
        super.setup();
        getCompetitors().forEach(data -> lapMap.put(data, 0));
        Bukkit.broadcast(Component.translatable("event.track.laps").args(Component.text(laps)).color(NamedTextColor.GREEN));
    }

    @Override
    public void start() {
        super.start();
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".start_command")));
        time = 0f;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (getStage() != Stage.STARTED) {
                    cancel();
                    return;
                }
                time++;
            }
        }.runTaskTimer(PLUGIN, 1L, 1L));
    }

    @Override
    public void end(boolean force) {
        super.end(force);
        if (force) return;
        Component c = Component.text().build();
        for (int i = 0; i < leaderboard.size();) {
            CompetitorData data = leaderboard.get(i++);
            c = c.append(Component.translatable("event.track.rank").args(Component.text(i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i < leaderboard.size()) c = c.appendNewline();
            if (i <= 3) data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @EventHandler
    public void onEvent(@NotNull PlayerMoveEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
            CompetitorData data = Competitions.getCompetitor(p.getUniqueId());
            if (leaderboard.contains(data) || !lapMap.containsKey(data)) return;
            Location loc = p.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            if (loc.getBlock().getType() == FINISH_LINE) {
                lapMap.put(data, lapMap.get(data) + 1);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
                if (lapMap.get(data) < laps) {
                    p.teleport(getLocation());
                    p.setBedSpawnLocation(getLocation(), true);
                    onCompletedLap(p);
                    Bukkit.broadcast(Component.translatable("event.track.competitor.completed_lap").args(p.displayName()).color(NamedTextColor.YELLOW));
                } else {
                    record.put(data, time / 20f);
                    p.setGameMode(GameMode.SPECTATOR);
                    leaderboard.add(Competitions.getCompetitor(p.getUniqueId()));
                    onRaceFinish(p);
                    Bukkit.broadcast(Component.translatable("event.track.competitor.completed_all").args(p.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW));
                    if (leaderboard.size() == getCompetitors().size()) {
                        if (task != null && !task.isCancelled()) task.cancel();
                        PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.all_completed"));
                        end(false);
                        return;
                    }
                    if (leaderboard.size() >= 3 && !endCountdown) {
                        endCountdown = true;
                        Bukkit.broadcast(Component.translatable("event.track.end.countdown.notice").args(Component.text(PLUGIN.getConfig().getInt("event_end_countdown"))));
                        task = addRunnable(new BukkitRunnable() {
                            int i = PLUGIN.getConfig().getInt("event_end_countdown");

                            @Override
                            public void run() {
                                if (i > 0)
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
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
                p.sendMessage(Component.translatable("competitor.practice.finished").args(getName()));
                onCompletedLap(p);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            }
        }
    }

    protected void onCompletedLap(@NotNull Player player) {
    }

    protected void onRaceFinish(@NotNull Player player) {
    }

    @Override
    public int getMaxLaps() {
        return laps;
    }

    @Override
    public final List<CompetitorData> getLeaderboard() {
        return leaderboard;
    }
}
