package org.macausmp.sportsday.customize;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

public enum ParticleEffect implements ComponentLike {
    GLOW("customize.particle.glow", Material.GLOWSTONE_DUST, new ParticleBuilder(Particle.GLOW)),
    ENCHANT("customize.particle.enchant", Material.ENCHANTING_TABLE, new ParticleBuilder(Particle.ENCHANT)),
    LAVA("customize.particle.lava", Material.LAVA_BUCKET, new ParticleBuilder(Particle.LAVA)),
    WATER("customize.particle.water", Material.WATER_BUCKET, new ParticleBuilder(Particle.FALLING_WATER)),
    CHERRY("customize.particle.cherry", Material.CHERRY_LEAVES, new ParticleBuilder(Particle.CHERRY_LEAVES)),
    HEART("customize.particle.heart", Material.GOLDEN_APPLE, new ParticleBuilder(Particle.HEART)),
    NOTE("customize.particle.note", Material.NOTE_BLOCK, new ParticleBuilder(Particle.NOTE)),
    GREEN_STAR("customize.particle.green_star", Material.EMERALD, new ParticleBuilder(Particle.HAPPY_VILLAGER)),
    BLOOD("customize.particle.blood", Material.REDSTONE,
            new ParticleBuilder(Particle.BLOCK).data(Material.REDSTONE_BLOCK.createBlockData()));

    private final Component name;
    private final Particle particle;
    private final Object data;
    private final Material material;

    ParticleEffect(String code, Material material, @NotNull ParticleBuilder builder) {
        this.name = TextUtil.text(Component.translatable(code));
        this.particle = builder.particle();
        this.data = builder.data();
        this.material = material;
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public void play(@NotNull Player player, @NotNull Location location) {
        player.spawnParticle(particle, location, 1, 0.3, 0.3, 0.3, data);
    }
}
