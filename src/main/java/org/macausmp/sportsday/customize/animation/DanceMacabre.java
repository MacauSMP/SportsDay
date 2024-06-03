package org.macausmp.sportsday.customize.animation;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DanceMacabre extends Animation {
    private static final Material[] MATERIALS = new Material[]{Material.NETHERRACK, Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.CRIMSON_NYLIUM, Material.NETHER_WART_BLOCK};
    private static final EntityType[] MOBS = new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER_SKELETON,
            EntityType.WITCH, EntityType.CREEPER, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN};
    private int timer = 0;
    private final Location location;
    private final World world;
    private final long time;
    private final Difficulty difficulty;
    private final Random random = new Random();
    private final List<Mob> mobs = new ArrayList<>();
    private final Set<Location> locationSet = new HashSet<>();

    public DanceMacabre(@NotNull Player player) {
        this.location = player.getLocation();
        this.world = player.getWorld();
        this.time = world.getTime();
        this.difficulty = world.getDifficulty();
        this.world.setDifficulty(Difficulty.EASY);
        this.world.setTime(18000);
    }

    @Override
    public void run() {
        if (++timer % 5 == 0) {
            Location loc = location.clone();
            loc.add(random.nextInt(timer) - (timer >> 1), 0, random.nextInt(timer) - (timer >> 1));
            Mob mob = (Mob) world.spawnEntity(loc.toHighestLocation().subtract(0, 1, 0),
                    Objects.requireNonNull(MOBS[random.nextInt(MOBS.length)]));
            mob.setSilent(true);
            mob.setAggressive(false);
            mob.setVelocity(new Vector(0, 0.6, 0));
            mob.setNoDamageTicks(20);
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 5, false, false));
            mobs.add(mob);
        }
        mobs.forEach(mob -> {
            Location loc = mob.getEyeLocation();
            loc.setYaw(loc.getYaw() + 10);
            mob.lookAt(loc.add(loc.getDirection()));
            mob.setAggressive(false);
            mob.setTarget(null);
            if (mob.isOnGround())
                mob.setJumping(true);
        });
        Set<BlockState> set = new HashSet<>();
        final int m = timer * timer, m1 = (timer - 2) * (timer - 2);
        for (int x = -timer; x < timer; x++) {
            for (int z = -timer; z < timer; z++) {
                int xz = x * x + z * z;
                if (xz >= m || xz <= m1)
                    continue;
                Location loc = location.clone().add(x, 0, z).toHighestLocation();
                if (locationSet.contains(loc))
                    continue;
                for (int y = 0, max = random.nextInt(2) + 1; y < max; y++) {
                    BlockState state = loc.subtract(0, y, 0).getBlock().getState();
                    if (state.getType().isOccluding()) {
                        state.setType(MATERIALS[random.nextInt(3) == 0 ? random.nextInt(MATERIALS.length) : 0]);
                        set.add(state);
                        locationSet.add(state.getLocation());
                    }
                    if (state.getType() == Material.WATER) {
                        state.setType(Material.LAVA);
                        set.add(state);
                        locationSet.add(state.getLocation());
                    }
                }
            }
        }
        Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChanges(set));
    }

    @Override
    public void stop() {
        mobs.forEach(Entity::remove);
        world.setDifficulty(difficulty);
        world.setTime(time);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChanges(locationSet.stream()
                .map(loc -> loc.getBlock().getState()).collect(Collectors.toSet())));
    }
}
