package org.macausmp.sportsday.competition;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.ItemUtil;

public class ElytraRacing extends AbstractTrackEvent {
    private static final ItemStack ELYTRA = elytra();
    private static final ItemStack FIREWORK = firework();

    public ElytraRacing() {
        super("elytra_racing", Material.ELYTRA);
    }

    @Override
    protected void onSetup() {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().getInventory().setItem(EquipmentSlot.CHEST, ELYTRA));
    }

    @Override
    protected void onStart() {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().getInventory().setItem(0, FIREWORK));
    }

    @Override
    protected void onEnd() {}

    @Override
    protected void onPractice(@NotNull Player player) {
        player.getInventory().setItem(EquipmentSlot.CHEST, ELYTRA);
        player.getInventory().setItem(0, FIREWORK);
    }

    @EventHandler
    public void onElytraBoost(@NotNull PlayerElytraBoostEvent e) {
        e.setShouldConsume(false);
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p))
            return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET)
            e.setCancelled(true);
    }

    private static @NotNull ItemStack elytra() {
        ItemStack elytra = ItemUtil.setBind(new ItemStack(Material.ELYTRA));
        elytra.editMeta(meta -> meta.setUnbreakable(true));
        return elytra;
    }

    private static @NotNull ItemStack firework() {
        ItemStack firework = ItemUtil.setBind(new ItemStack(Material.FIREWORK_ROCKET, 64));
        firework.editMeta(FireworkMeta.class, meta -> meta.setPower(3));
        return firework;
    }
}
