package org.macausmp.sportsday.competition;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Parkour extends TrackEvent {
    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY,
            PotionEffect.INFINITE_DURATION, 0, false, false, false);

    public Parkour() {
        super("parkour", Material.LEATHER_BOOTS);
    }

    @Override
    protected void onSetup() {}

    @Override
    protected void onStart() {
        Competitions.getOnlineContestants().forEach(d -> {
            Player p = d.getPlayer();
            p.addPotionEffect(INVISIBILITY);
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
        });
    }

    @Override
    protected void onEnd() {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().clearActivePotionEffects());
    }

    @Override
    protected void onPractice(@NotNull Player player) {
        player.addPotionEffect(INVISIBILITY);
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
    }

    @Override
    protected void onCompletedLap(@NotNull Player player) {}

    @Override
    protected void onRaceFinish(@NotNull Player p) {
        p.clearActivePotionEffects();
    }

    @EventHandler
    public void onRespawn(@NotNull PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (predicate.test(p)) {
            addRunnable(new BukkitRunnable() {
                @Override
                public void run() {
                    p.addPotionEffect(INVISIBILITY);
                    cancel();
                }
            }.runTaskLater(PLUGIN, 5L));
        }
    }
}
