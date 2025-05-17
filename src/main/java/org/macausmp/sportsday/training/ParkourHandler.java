package org.macausmp.sportsday.training;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.sport.Sport;

public class ParkourHandler extends TrackSportsHandler {
    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY,
            PotionEffect.INFINITE_DURATION, 0, false, false, false);

    public ParkourHandler(Sport sport) {
        super(sport);
    }

    @Override
    public void equip(@NotNull Player player) {
        player.addPotionEffect(INVISIBILITY);
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
    }
}
