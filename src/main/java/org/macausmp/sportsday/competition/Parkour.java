package org.macausmp.sportsday.competition;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Parkour extends AbstractTrackEvent {
    public Parkour() {
        super("parkour");
    }

    @Override
    protected void onSetup() {}

    @Override
    protected void onStart() {
        Competitions.getOnlineContestants().forEach(d -> {
            Player p = d.getPlayer();
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
        });
    }

    @Override
    protected void onEnd(boolean force) {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().clearActivePotionEffects());
    }

    @Override
    protected void onPractice(@NotNull Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
    }

    @Override
    protected void onRaceFinish(@NotNull Player p) {
        p.clearActivePotionEffects();
    }

    @EventHandler
    public void onRespawn(@NotNull PlayerRespawnEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStatus() == Status.STARTED && Competitions.isContestant(p)
                && !getLeaderboard().contains(Competitions.getContestant(p.getUniqueId()))
                || inPractice(p, this)) {
            addRunnable(new BukkitRunnable() {
                @Override
                public void run() {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                    cancel();
                }
            }.runTaskLater(PLUGIN, 5L));
        }
    }
}
