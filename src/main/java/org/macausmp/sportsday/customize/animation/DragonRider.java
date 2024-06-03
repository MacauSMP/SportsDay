package org.macausmp.sportsday.customize.animation;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DragonRider extends Animation {
    private final Player player;
    private final World world;
    private final boolean rule;
    private final EnderDragon dragon;

    public DragonRider(@NotNull Player player) {
        this.player = player;
        this.world = player.getWorld();
        this.rule = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.MOB_GRIEFING));
        this.player.setGameMode(GameMode.ADVENTURE);
        this.world.setGameRule(GameRule.MOB_GRIEFING, false);
        this.dragon = player.getWorld().spawn(player.getLocation(), EnderDragon.class);
        this.dragon.setInvulnerable(true);
    }

    @Override
    public void run() {
        dragon.addPassenger(player);
        // set phase 2 times in a tick to have the dragon follow the player's direction
        dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
        dragon.setPhase(EnderDragon.Phase.LAND_ON_PORTAL);
        dragon.setPodium(player.getLocation().add(player.getLocation().getDirection().multiply(50)));
        dragon.setVelocity(player.getLocation().getDirection());
    }

    @Override
    public void stop() {
        dragon.remove();
        world.setGameRule(GameRule.MOB_GRIEFING, rule);
    }
}
