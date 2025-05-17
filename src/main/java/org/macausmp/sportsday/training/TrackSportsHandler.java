package org.macausmp.sportsday.training;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Track;
import org.macausmp.sportsday.venue.TrackPoint;
import org.macausmp.sportsday.venue.Venue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class TrackSportsHandler extends SportsTrainingHandler {
    private final Map<UUID, BukkitTask> runningMap = new HashMap<>();
    private final Map<UUID, Integer> checkpointMap = new HashMap<>();

    public TrackSportsHandler(Sport sport) {
        super(sport);
    }

    @Override
    public void joinTraining(@NotNull Player player, @NotNull Venue venue) {
        super.joinTraining(player, venue);
        player.getInventory().setItem(2, ItemUtil.CHECKPOINT);
        player.getInventory().setItem(5, ItemUtil.RESET);
    }

    @Override
    public void leaveTraining(@NotNull UUID uuid) {
        if (runningMap.containsKey(uuid)) {
            runningMap.get(uuid).cancel();
            runningMap.remove(uuid);
            checkpointMap.remove(uuid);
        }
        super.leaveTraining(uuid);
    }

    protected void teleport(@NotNull Player player, @NotNull Location location) {
        player.teleportAsync(location);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!isTraining(p))
            return;
        Track track = (Track) getTrainingVenue(p);
        UUID uuid = p.getUniqueId();
        if (track.getStartPoint().overlaps(p) && !runningMap.containsKey(uuid)) {
            checkpointMap.put(uuid, 0);
            runningMap.put(uuid, new BukkitRunnable() {
                int time = 0;

                @Override
                public void run() {
                    p.sendActionBar(Component.text("%.2f".formatted(time / 20f)).color(NamedTextColor.YELLOW));
                    if (track.getEndPoint().overlaps(p) && checkpointMap.get(uuid) == track.getCheckPoints().size()) {
                        p.sendMessage(Component.translatable("training.finished")
                                .arguments(Component.text(time / 20f).color(NamedTextColor.YELLOW), sport));
                        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"),
                                Sound.Source.MASTER, 1f, 1f));
                        checkpointMap.remove(uuid);
                        runningMap.get(uuid).cancel();
                        runningMap.remove(uuid);
                        cancel();
                        return;
                    }
                    ++time;
                }
            }.runTaskTimer(PLUGIN, 0L, 1L));
        }
        if (!runningMap.containsKey(uuid))
            return;
        int next = checkpointMap.get(uuid);
        if (next < track.getCheckPoints().size()) {
            for (int i = next + 1; i < track.getCheckPoints().size(); i++) {
                if (track.getCheckPoints().get(i).overlaps(p)) {
                    teleport(p, (next > 0 ? track.getCheckPoints().get(next - 1) : track.getStartPoint()).getLocation());
                    p.sendActionBar(Component.translatable("event.track.checkpoint_missed")
                            .color(NamedTextColor.RED));
                    p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER, 1f, 1f));
                    break;
                }
            }
            TrackPoint point = track.getCheckPoints().get(next);
            if (point.overlaps(p)) {
                p.setRespawnLocation(p.getLocation(), true);
                checkpointMap.put(uuid, ++next);
                p.sendMessage(Component.translatable("event.track.checkpoint")
                        .arguments(Component.text(next)).color(NamedTextColor.GREEN));
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"),
                        Sound.Source.MASTER, 1f, 1f));
            }
        }
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isTraining(p))
            return;
        ItemStack item = e.getItem();
        if (item == null)
            return;
        Track track = (Track) getTrainingVenue(p);
        UUID uuid = p.getUniqueId();
        if (ItemUtil.equals(item, ItemUtil.CHECKPOINT) && checkpointMap.containsKey(uuid)) {
            int i = checkpointMap.get(uuid);
            teleport(p, (i > 0 ? track.getCheckPoints().get(i - 1) : track.getStartPoint()).getLocation());
            p.sendMessage(Component.translatable("training.checkpoint"));
        } else if (ItemUtil.equals(item, ItemUtil.RESET)) {
            if (runningMap.containsKey(uuid)) {
                runningMap.get(uuid).cancel();
                runningMap.remove(uuid);
                checkpointMap.remove(uuid);
            }
            teleport(p, track.getLocation());
            p.sendMessage(Component.translatable("training.reset"));
        }
    }
}
