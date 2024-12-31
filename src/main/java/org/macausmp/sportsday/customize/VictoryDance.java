package org.macausmp.sportsday.customize;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.customize.animation.*;
import org.macausmp.sportsday.util.TextUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public enum VictoryDance implements ComponentLike {
    DRAGON_RIDER("customize.victory_dance.dragon_rider", Material.DRAGON_HEAD, DragonRider::new),
    MADE_IN_HEAVEN("customize.victory_dance.made_in_heaven", Material.CLOCK, MadeInHeaven::new),
    DANCE_MACABRE("customize.victory_dance.dance_macabre", Material.WITHER_SKELETON_SKULL, DanceMacabre::new),
    VIOLENT_STORM("customize.victory_dance.violent_storm", Material.LIGHTNING_ROD, ViolentStorm::new);

    private static final Map<UUID, Animation> ANIMATION_MAP = new HashMap<>();
    private final Component name;
    private final Material material;
    private final Function<Player, Animation> function;

    VictoryDance(String code, Material material, Function<Player, Animation> function) {
        this.name = TextUtil.text(Component.translatable(code));
        this.material = material;
        this.function = function;
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public void play(@NotNull Player player) {
        ANIMATION_MAP.put(player.getUniqueId(), function.apply(player));
        Animation animation = ANIMATION_MAP.get(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (animation.isCancelled()) {
                    cancel();
                    return;
                }
                animation.run();
            }
        }.runTaskTimer(SportsDay.getInstance(), 0, 1);
    }

    public void stop(UUID uuid) {
        Animation animation = ANIMATION_MAP.get(uuid);
        if (animation == null)
            return;
        animation.stop();
        animation.cancel();
    }
}
