package org.macausmp.sportsday;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.AbstractEvent;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.customize.GraffitiSpray;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.customize.GraffitiSprayGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SportsDayListener implements Listener {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final NamespacedKey GRAFFITI = new NamespacedKey(PLUGIN, "graffiti_frame");
    private static final Set<UUID> EASTER_EGG = new HashSet<>();

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        ParticleEffect effect = PlayerCustomize.getWalkingEffect(p);
        if (effect != null) {
            Location loc = p.getLocation().clone();
            loc.setY(loc.y() + 0.3);
            effect.play(p, loc);
        }
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            IEvent current = Competitions.getCurrentEvent();
            if (current != null && current.getStatus() == Status.STARTED || AbstractEvent.inPractice(player))
                return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(@NotNull PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE)
            return;
        if (ItemUtil.isBind(e.getItemDrop().getItemStack()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSwapItem(@NotNull PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE)
            return;
        if (ItemUtil.isBind(e.getOffHandItem()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onQuitPractice(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null)
            return;
        Player p = e.getPlayer();
        if (!ItemUtil.equals(e.getItem(), ItemUtil.LEAVE_PRACTICE))
            return;
        AbstractEvent.leavePractice(p);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @EventHandler
    public void onSpray(@NotNull PlayerInteractEvent e) {
        if (e.getItem() != null && ItemUtil.equals(e.getItem(), ItemUtil.SPRAY)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                p.openInventory(new GraffitiSprayGUI(p).getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                return;
            }
            if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                return;
            if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                p.sendActionBar(Component.translatable("item.spray.cooldown")
                        .arguments(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f))
                        .color(NamedTextColor.YELLOW));
                return;
            }
            GraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
            if (graffiti == null || Bukkit.getMap(graffiti.ordinal()) == null)
                return;
            Block b = p.getTargetBlockExact(4);
            BlockFace f = p.getTargetBlockFace(4);
            if (b == null || !b.getType().isOccluding() || f == null)
                return;
            Location loc = b.getLocation().add(f.getDirection());
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.ordinal())));
            loc.getWorld().playSound(Sound.sound(Key.key("minecraft:use_spray"),
                    Sound.Source.MASTER, 1f, 1f), loc.x(), loc.y(), loc.z());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOp())
                        p.setCooldown(ItemUtil.SPRAY.getType(), 600);
                    ItemFrame frame = loc.getWorld().spawn(loc, ItemFrame.class);
                    // Remove item frame that should not appear in an illegal position
                    if (!frame.getLocation().add(frame.getFacing().getOppositeFace().getDirection()).getBlock().getType().isOccluding()) {
                        frame.remove();
                        return;
                    }
                    frame.setVisible(false);
                    frame.setInvulnerable(true);
                    frame.setFixed(true);
                    frame.setItem(map, false);
                    frame.getPersistentDataContainer().set(GRAFFITI, PersistentDataType.BOOLEAN, true);
                }
            }.runTaskLater(PLUGIN, 30L);
        }
    }

    @EventHandler
    public void onChangeSpray(@NotNull PlayerInteractEntityEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtil.equals(item, ItemUtil.SPRAY) && e.getRightClicked() instanceof ItemFrame frame) {
            if (!frame.getPersistentDataContainer().has(GRAFFITI))
                return;
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                p.sendActionBar(Component.translatable("item.spray.cooldown")
                        .arguments(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f))
                        .color(NamedTextColor.YELLOW));
                return;
            }
            GraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
            if (graffiti == null || Bukkit.getMap(graffiti.ordinal()) == null)
                return;
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.ordinal())));
            Location loc = frame.getLocation();
            loc.getWorld().playSound(Sound.sound(Key.key("minecraft:use_spray"),
                    Sound.Source.MASTER, 1f, 1f), loc.x(), loc.y(), loc.z());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOp())
                        p.setCooldown(ItemUtil.SPRAY.getType(), 600);
                    frame.setItem(map, false);
                }
            }.runTaskLater(PLUGIN, 30L);
        }
    }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p) || e.getClickedInventory() == null)
            return;
        if (e.getInventory().getHolder() instanceof PluginGUI gui) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || !ItemUtil.hasID(item))
                return;
            gui.click(e, p, item);
        } else {
            if (p.isOp() || p.getGameMode() == GameMode.CREATIVE)
                return;
            ItemStack current = e.getCurrentItem();
            ItemStack button = e.getHotbarButton() == -1 ? null : p.getInventory().getItem(e.getHotbarButton());
            if (current != null && ItemUtil.isBind(current) || button != null && ItemUtil.isBind(button))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof PluginGUI gui)
            gui.onClose();
    }

    @EventHandler
    public void onOpenGUI(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (ItemUtil.hasID(e.getItem()))
            e.setCancelled(true);
        Player p = e.getPlayer();
        if (ItemUtil.equals(e.getItem(), ItemUtil.MENU)) {
            p.openInventory(new MenuGUI().getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.CUSTOMIZE)) {
            p.openInventory(new CustomizeMenuGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.OP_BOOK)) {
            if (p.isOp()) {
                p.openInventory(new CompetitionMenuGUI().getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                return;
            }
            // Easter egg, happen if player use the book without op permission
            if (EASTER_EGG.contains(p.getUniqueId()))
                return;
            EASTER_EGG.add(p.getUniqueId());
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        EASTER_EGG.remove(p.getUniqueId());
                        cancel();
                        return;
                    }
                    if (i >= 3 && i < 10) {
                        p.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 20, 0.2, 0.5, 0.2,
                                Material.REDSTONE_BLOCK.createBlockData());
                    }
                    if (i == 0) {
                        p.damage(5);
                        p.sendMessage(Component.translatable("item.op_book_easter_egg1").arguments(e.getItem().displayName()));
                    } else if (i == 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 2, false, false));
                    } else if (i == 8) {
                        p.sendMessage(Component.translatable("item.op_book_easter_egg2"));
                    } if (i >= 10) {
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                        p.setHealth(0);
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                        EASTER_EGG.remove(p.getUniqueId());
                        cancel();
                    }
                    i++;
                }
            }.runTaskTimer(PLUGIN, 0L, 10L);
        }
    }
}
