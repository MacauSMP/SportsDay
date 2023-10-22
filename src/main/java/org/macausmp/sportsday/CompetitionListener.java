package org.macausmp.sportsday;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoRound;
import org.macausmp.sportsday.customize.CustomizeGraffitiSpray;
import org.macausmp.sportsday.customize.CustomizeParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.gui.competition.CompetitorListGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.customize.GraffitiSprayGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CompetitionListener implements Listener {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final List<UUID> SPAWNPOINT_LIST = new ArrayList<>();
    public static final Material CHECKPOINT = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("checkpoint_block")));
    public static final Material DEATH = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("death_block")));
    public static final NamespacedKey GRAFFITI = Objects.requireNonNull(NamespacedKey.fromString("graffiti_frame", PLUGIN));
    private static final List<UUID> EASTER_TRIGGER = new ArrayList<>();

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(0, ItemUtil.MENU);
        p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        IEvent current = Competitions.getCurrentEvent();
        if (current == null || current.getStage() != Stage.STARTED) return;
        if (!Competitions.containPlayer(p)) return;
        CompetitionInfoGUI.updateGUI();
        CompetitorListGUI.updateGUI();
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        CompetitionInfoGUI.updateGUI();
        CompetitorListGUI.updateGUI();
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        CustomizeParticleEffect effect = PlayerCustomize.getWalkingEffect(p);
        if (effect != null) {
            Location loc = p.getLocation().clone();
            loc.setY(loc.y() + 0.3);
            p.spawnParticle(effect.getParticle(), loc, 1, 0.3f, 0.3f, 0.3f, effect.getData());
        }
        IEvent current = Competitions.getCurrentEvent();
        if ((current instanceof ITrackEvent && current.getStage() == Stage.STARTED && Competitions.containPlayer(p)) || AbstractEvent.getPracticeEvent(p) instanceof ITrackEvent) {
            Location loc = e.getTo().clone();
            loc.setY(loc.getY() - 0.5f);
            spawnpoint(p, loc);
            if (SPAWNPOINT_LIST.contains(p.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT) SPAWNPOINT_LIST.remove(p.getUniqueId());
            if (loc.getWorld().getBlockAt(loc).getType() == DEATH) p.setHealth(0);
        }
    }

    public static void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        if (!SPAWNPOINT_LIST.contains(player.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() == CHECKPOINT) {
            SPAWNPOINT_LIST.add(player.getUniqueId());
            player.setBedSpawnLocation(player.getLocation(), true);
            player.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            player.sendActionBar(Component.text("Checkpoint").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
        }
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            IEvent current = Competitions.getCurrentEvent();
            if (current == Competitions.SUMO) {
                SumoRound round = ((Sumo) current).getSumoStage().getCurrentRound();
                boolean b = round != null && round.getStatus() == SumoRound.RoundStatus.STARTED && round.contain(player) && round.contain(damager);
                if (b || AbstractEvent.inPractice(player, Competitions.SUMO)) {
                    e.setDamage(0);
                } else {
                    e.setCancelled(true);
                }
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            IEvent current = Competitions.getCurrentEvent();
            if (current == null || !Competitions.containPlayer(player) || current.getStage() == Stage.STARTED) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || (p.getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(p))) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || (p.getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(p))) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(@NotNull PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || (p.getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(p))) return;
        if (ItemUtil.equals(e.getItemDrop().getItemStack(), ItemUtil.MENU) || ItemUtil.equals(e.getItemDrop().getItemStack(), ItemUtil.CUSTOMIZE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuitPractice(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        if (ItemUtil.equals(e.getItem(), ItemUtil.QUIT_PRACTICE)) {
            AbstractEvent.quitPractice(p);
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        }
    }

    @EventHandler
    public void onSpray(@NotNull PlayerInteractEvent e) {
        if (e.getItem() != null && ItemUtil.equals(e.getItem(), ItemUtil.SPRAY)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                p.openInventory(new GraffitiSprayGUI(p).getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                    p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                    return;
                }
                CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
                if (graffiti == null || Bukkit.getMap(graffiti.getId()) == null) return;
                Block b = p.getTargetBlockExact(4);
                BlockFace f = p.getTargetBlockFace(4);
                if (b != null && b.getType().isOccluding() && f != null) {
                    Location loc = b.getLocation().add(f.getDirection());
                    ItemStack map = new ItemStack(Material.FILLED_MAP);
                    map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.getId())));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:use_spray"), Sound.Source.MASTER, 1f, 1f));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!p.isOp()) p.setCooldown(ItemUtil.SPRAY.getType(), 600);
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
        }
    }

    @EventHandler
    public void onChangeSpray(@NotNull PlayerInteractEntityEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtil.equals(item, ItemUtil.SPRAY) && e.getRightClicked() instanceof ItemFrame frame) {
            if (!frame.getPersistentDataContainer().has(GRAFFITI)) return;
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                return;
            }
            CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
            if (graffiti == null || Bukkit.getMap(graffiti.getId()) == null) return;
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.getId())));
            Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:use_spray"), Sound.Source.MASTER, 1f, 1f));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOp()) p.setCooldown(ItemUtil.SPRAY.getType(), 600);
                    frame.setItem(map, false);
                }
            }.runTaskLater(PLUGIN, 30L);
        }
    }

    @EventHandler
    public void onOpenGUI(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (ItemUtil.hasID(e.getItem())) e.setCancelled(true);
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
            if (EASTER_TRIGGER.contains(p.getUniqueId())) return;
            EASTER_TRIGGER.add(p.getUniqueId());
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        cancel();
                        return;
                    }
                    if (i >= 3 && i < 10) {
                        p.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 20, 0.2, 0.5, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    }
                    if (i == 0) {
                        p.damage(5);
                        p.sendMessage(Component.translatable("item.op_book_easter_egg1").args(e.getItem().displayName()));
                    } else if (i == 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 2, false, false));
                    } else if (i == 8) {
                        p.sendMessage(Component.translatable("item.op_book_easter_egg2"));
                    } if (i == 10) {
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                        p.setHealth(0);
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                        EASTER_TRIGGER.remove(p.getUniqueId());
                        cancel();
                    }
                    i++;
                }
            }.runTaskTimer(PLUGIN, 0L, 10L);
        }
    }
}
