package org.macausmp.sportsday.training;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JavelinThrowHandler extends SportsTrainingHandler {
    private static final ItemStack TRIDENT = trident();
    private static final NamespacedKey TRAINING_TRIDENT = new NamespacedKey(PLUGIN, "training_trident");
    private final Map<UUID, Vector> resultMap = new HashMap<>();

    private static @NotNull ItemStack trident() {
        String display = "item.sportsday.javelin";
        Component lore = Component.translatable("enchantment.sportsday.range")
                .arguments(Component.translatable("enchantment.level.5")).color(NamedTextColor.GRAY);
        ItemStack trident = ItemUtil.setBind(ItemUtil.item(Material.TRIDENT, null, display, lore));
        trident.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.setEnchantmentGlintOverride(true);
        });
        return trident;
    }

    public JavelinThrowHandler(Sport sport) {
        super(sport);
    }

    @Override
    public void equip(@NotNull Player player) {
        player.getInventory().setItem(0, TRIDENT);
    }

    @Override
    public void leaveTraining(@NotNull UUID uuid) {
        super.leaveTraining(uuid);
    }

    @EventHandler
    public void onThrow(@NotNull ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (!isTraining(p))
                return;
            resultMap.put(trident.getUniqueId(), p.getLocation().toVector());
            trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            trident.setCustomNameVisible(true);
            trident.customName(Component.translatable("event.javelin.javelin_name")
                    .arguments(p.displayName(), Component.text()));
            trident.getPersistentDataContainer().set(TRAINING_TRIDENT, PersistentDataType.BOOLEAN, true);
            final ParticleEffect effect = PlayerCustomize.getProjectileTrail(p);
            if (effect != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (trident.isDead() || trident.isOnGround()) {
                            cancel();
                            return;
                        }
                        effect.play(p, trident.getLocation());
                    }
                }.runTaskTimer(PLUGIN, 0, 1L);
            }
        }
    }

    @EventHandler
    public void onArrived(@NotNull ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (isTraining(p)) {
                UUID uuid = trident.getUniqueId();
                Vector vec = resultMap.get(uuid);
                if (vec == null)
                    return;
                double distance = vec.distance(trident.getLocation().toVector());
                if (p.isOnline()) {
                    p.sendMessage(Component.translatable("event.javelin.practice_result")
                            .arguments(Component.text(distance)));
                    p.getInventory().setItem(0, TRIDENT);
                }
                resultMap.remove(uuid);
            }
            if (trident.getPersistentDataContainer().has(TRAINING_TRIDENT))
                trident.remove();
        }
    }
}
