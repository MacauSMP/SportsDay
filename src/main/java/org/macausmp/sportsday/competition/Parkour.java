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
    protected void onSetup() {
        getCompetitors().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    protected void onStart() {
        Competitions.getOnlineCompetitors().forEach(d -> {
            d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            d.getPlayer().getInventory().setHelmet(null);
            d.getPlayer().getInventory().setChestplate(null);
            d.getPlayer().getInventory().setLeggings(null);
        });
    }

    @Override
    protected void onEnd(boolean force) {
        Competitions.getOnlineCompetitors().forEach(d -> {
            d.getPlayer().setCollidable(true);
            d.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }

    @Override
    protected void onPractice(@NotNull Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
    }

    @Override
    protected void onRaceFinish(@NotNull Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        if (event != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (getLeaderboard().contains(Competitions.getCompetitor(p.getUniqueId()))) return;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                cancel();
            }
        }.runTaskLater(PLUGIN, 5L));
    }
}
