package org.macausmp.sportsday.training;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Venue;

import java.util.*;

public class SumoHandler extends SportsTrainingHandler {
    private final List<Player> queue = new ArrayList<>();
    private final Set<SumoRoom> rooms = new HashSet<>();

    public SumoHandler(Sport sport) {
        super(sport);
    }

    @Override
    public void equip(@NotNull Player player) {

    }

    @Override
    public void joinTraining(@NotNull Player player, @NotNull Venue venue) {
        super.joinTraining(player, venue);
        queue.add(player);
        if (queue.size() > 1 && rooms.isEmpty()) {
            rooms.add(new SumoRoom(queue.removeFirst(), queue.removeFirst()));
        }
    }

    @Override
    public void leaveTraining(@NotNull UUID uuid) {
        super.leaveTraining(uuid);
        queue.removeIf(p -> p.getUniqueId().equals(uuid));

    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!isTraining(p))
            return;
        for (SumoRoom room : rooms) {
            if (room.contain(p)) {
                if (room.ended)
                    return;
                if (!room.started) {
                    e.setCancelled(true);
                    return;
                }
                if (p.isInWater()) {
                    room.ended = true;
                    rooms.remove(room);
                    queue.add(room.p1);
                    queue.add(room.p2);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            if (!isTraining(player) || !isTraining(damager))
                return;
            for (SumoRoom room : rooms) {
                if (room.contain(player) && room.contain(damager)) {
                    if (!room.started || room.ended) {
                        e.setCancelled(true);
                        return;
                    }
                    e.setDamage(0);
                    return;
                }
            }
            e.setCancelled(true);
        }
    }

    private static final class SumoRoom {
        private final Player p1;
        private final Player p2;
        private boolean started = false;
        private boolean ended = false;

        public SumoRoom(Player p1, Player p2) {
            this.p1 = p1;
            this.p2 = p2;
            new BukkitRunnable() {
                int i = 3;

                @Override
                public void run() {
                    p1.sendTitlePart(TitlePart.TITLE, Component.text(i).color(NamedTextColor.YELLOW));
                    p2.sendTitlePart(TitlePart.TITLE, Component.text(i).color(NamedTextColor.YELLOW));
                    if (--i <= 0) {
                        started = true;
                        p1.sendTitlePart(TitlePart.TITLE, Component.text("GO").color(NamedTextColor.YELLOW));
                        p2.sendTitlePart(TitlePart.TITLE, Component.text("GO").color(NamedTextColor.YELLOW));
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L);
        }

        public boolean contain(Player player) {
            return player == p1 || player == p2;
        }
    }
}
