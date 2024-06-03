package org.macausmp.sportsday.customize.animation;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MadeInHeaven extends Animation {
    private int i = 0;
    private final World world;
    private final long ft;
    private final boolean cycle;

    public MadeInHeaven(@NotNull Player player) {
        this.world = player.getWorld();
        this.ft = world.getFullTime();
        this.cycle = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
    }

    @Override
    public void run() {
        world.setFullTime(ft + i++ * 500L % 24000);
    }

    @Override
    public void stop() {
        world.setFullTime(ft);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, cycle);
    }
}
