package org.macausmp.sportsday.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Track;

public class ParkourEvent extends TrackEvent {
    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY,
            PotionEffect.INFINITE_DURATION, 0, false, false, false);

    public ParkourEvent(@NotNull Sport sport, @NotNull Track track, @Nullable PersistentDataContainer save) {
        super(sport, track, save);
    }

    @Override
    protected void onRacingStart() {
        getContestants().forEach(d -> {
            Player p = d.getPlayer();
            p.addPotionEffect(INVISIBILITY);
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
        });
    }

    @Override
    protected void onClose() {
        getContestants().forEach(d -> d.getPlayer().clearActivePotionEffects());
    }

    @Override
    protected void onRaceFinish(@NotNull Player player) {
        player.clearActivePotionEffects();
    }

    @EventHandler
    public void onRespawn(@NotNull PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p))
            return;
        addTask(new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(INVISIBILITY);
                p.getInventory().setHelmet(null);
                p.getInventory().setChestplate(null);
                p.getInventory().setLeggings(null);
            }
        }.runTaskLater(PLUGIN, 5L));
    }
}
