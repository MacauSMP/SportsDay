package org.macausmp.sportsday.customize;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

public enum CustomizeParticleEffect {
    GLOW("customize.particle.glow", new ParticleBuilder(Particle.GLOW), Material.GLOWSTONE_DUST),
    ENCHANT("customize.particle.enchant", new ParticleBuilder(Particle.ENCHANTMENT_TABLE), Material.ENCHANTING_TABLE),
    LAVA("customize.particle.lava", new ParticleBuilder(Particle.LAVA), Material.LAVA_BUCKET),
    WATER("customize.particle.water", new ParticleBuilder(Particle.FALLING_WATER), Material.WATER_BUCKET),
    CHERRY("customize.particle.cherry", new ParticleBuilder(Particle.CHERRY_LEAVES), Material.CHERRY_LEAVES),
    HEART("customize.particle.heart", new ParticleBuilder(Particle.HEART), Material.GOLDEN_APPLE),
    NOTE("customize.particle.note", new ParticleBuilder(Particle.NOTE), Material.NOTE_BLOCK),
    GREEN_STAR("customize.particle.green_star", new ParticleBuilder(Particle.VILLAGER_HAPPY), Material.EMERALD),
    BLOOD("customize.particle.blood", new ParticleBuilder(Particle.BLOCK_DUST).data(Material.REDSTONE_BLOCK.createBlockData()), Material.REDSTONE);

    private final Component name;
    private final Particle particle;
    private final Object data;
    private final Material material;

    CustomizeParticleEffect(String code, @NotNull ParticleBuilder builder, Material material) {
        this.name = TextUtil.text(Component.translatable(code));
        this.particle = builder.particle();
        this.data = builder.data();
        this.material = material;
    }

    public Component getName() {
        return name;
    }

    public Particle getParticle() {
        return particle;
    }

    public <T> T getData() {
        //noinspection unchecked
        return (T) data;
    }

    public Material getMaterial() {
        return material;
    }
}
