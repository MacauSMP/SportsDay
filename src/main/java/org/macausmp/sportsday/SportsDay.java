package org.macausmp.sportsday;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.customize.GraffitiSpray;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.gui.competition.ContestantProfileGUI;
import org.macausmp.sportsday.gui.competition.ContestantsListGUI;
import org.macausmp.sportsday.gui.competition.event.EventGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.customize.GraffitiSprayGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    private static BossBar BOSSBAR;
    public static Team CONTESTANTS;
    public static Team REFEREES;
    public static Team AUDIENCES;
    public static NamespacedKey GRAFFITI;
    private static final Set<UUID> EASTER_EGG = new HashSet<>();

    /**
     * Get the instance of plugin.
     * @return instance of plugin
     */
    public static SportsDay getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        registerTranslation();
        saveDefaultConfig();
        reloadConfig();
        Competitions.load();
        BOSSBAR = BossBar.bossBar(Component.translatable("bossbar.title"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        CONTESTANTS = registerTeam("contestants", Component.translatable("role.contestants"), NamedTextColor.GREEN);
        REFEREES = registerTeam("referees", Component.translatable("role.referees"), NamedTextColor.GOLD);
        AUDIENCES = registerTeam("audiences", Component.translatable("role.audiences"), NamedTextColor.GRAY);
        GRAFFITI = new NamespacedKey(this, "graffiti_frame");
        new CommandManager().register();
        getServer().getPluginManager().registerEvents(this, this);
        Competitions.EVENTS.values().forEach(e -> getServer().getPluginManager().registerEvents(e, this));
        getServer().getWorlds().forEach(w -> {
            w.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
            w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            w.setGameRule(GameRule.DO_INSOMNIA, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            w.setGameRule(GameRule.KEEP_INVENTORY, true);
        });
        setTabList();
    }

    private void registerTranslation() {
        TranslationRegistry registry = TranslationRegistry.create(new NamespacedKey(this, "resource"));
        // There is no way to use an alternative language other than the en_US localization file that ships as part of the vanilla jar
        ResourceBundle bundle = ResourceBundle.getBundle("lang.Bundle", Locale.US, UTF8ResourceBundleControl.get());
        registry.registerAll(Locale.US, bundle, true);
        GlobalTranslator.translator().addSource(registry);
    }

    private @NotNull Team registerTeam(String name, Component display, NamedTextColor color) {
        Scoreboard scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
            team.displayName(TextUtil.text(display));
            team.color(color);
            team.prefix(TextUtil.text(Component.translatable("[%s]").arguments(display)));
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        return team;
    }

    private void setTabList() {
        long d = Math.round(20f - Instant.now().getNano() / 50000000f);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        new BukkitRunnable() {
            @Override
            public void run() {
                final Component head = getHeader();
                final Component time = Component.text(formatter.format(Instant.now()));
                for (Player p : getServer().getOnlinePlayers()) {
                    Component header = head;
                    if (Competitions.isContestant(p)) {
                        ContestantData data = Competitions.getContestant(p.getUniqueId());
                        Component number = Component.newline().append(Component.translatable("tablist.number")
                                .arguments(Component.text(data.getNumber())));
                        Component score = Component.newline().append(Component.translatable("tablist.score")
                                .arguments(Component.text(data.getScore())));
                        header = header.append(number).append(score);
                    }
                    Component ping = Component.newline().append(Component.translatable("tablist.ping")
                            .arguments(Component.text(p.getPing() + "ms")
                                    .color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));
                    Component footer = Component.translatable("tablist.local_time").arguments(time).append(ping);
                    p.sendPlayerListHeaderAndFooter(header, footer);
                    p.playerListName(p.teamDisplayName());
                }
            }

            private @NotNull Component getHeader() {
                SportingEvent event = Competitions.getCurrentEvent();
                Component competition = Component.newline().append(event == null
                        ? Component.translatable("tablist.idle")
                        : Component.translatable("tablist.current").arguments(event.getName(), event.getStatus()));
                Component count = Component.newline().append(Component.translatable("tablist.contestants_count")
                        .arguments(Component.text(Competitions.getOnlineContestants().size()),
                                Component.text(Competitions.getContestants().size())));
                return Component.translatable("tablist.title").append(competition).append(count);
            }
        }.runTaskTimer(instance, d, 20L);
    }

    @Override
    public void onDisable() {
        SportingEvent event = Competitions.getCurrentEvent();
        if (event != null) {
            CommandSender sender = getServer().getConsoleSender();
            if (event instanceof Savable)
                Competitions.saveEventData(sender);
            Competitions.terminate(sender);
            getServer().getOnlinePlayers().forEach(p -> {
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
            });
        }
        Competitions.save();
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BOSSBAR.addViewer(p);
        if (!p.hasPlayedBefore() || getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(p) == null) {
            SportsDay.AUDIENCES.addPlayer(p);
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            p.setGameMode(GameMode.ADVENTURE);
        }
        SportingEvent curr = Competitions.getCurrentEvent();
        if (curr == null) {
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        }
        if (p.getPersistentDataContainer().has(SportingEvent.IN_GAME)) {
            long last = Objects.requireNonNull(p.getPersistentDataContainer().get(SportingEvent.IN_GAME, PersistentDataType.LONG));
            if (curr == null || last != curr.getLastTime()) {
                if (p.isInsideVehicle())
                    Objects.requireNonNull(p.getVehicle()).remove();
                p.clearActivePotionEffects();
                p.setFireTicks(0);
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                p.setRespawnLocation(p.getWorld().getSpawnLocation(), true);
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
                p.getPersistentDataContainer().remove(SportingEvent.IN_GAME);
            }
        }
        if (!Competitions.isContestant(p))
            return;
        EventGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        ContestantProfileGUI.updateProfile(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        SportingEvent.leavePractice(p);
        if (!Competitions.isContestant(p))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                EventGUI.updateGUI();
                ContestantsListGUI.updateGUI();
                ContestantProfileGUI.updateProfile(p.getUniqueId());
            }
        }.runTaskLater(this, 1L);
    }

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
            SportingEvent current = Competitions.getCurrentEvent();
            if (current != null && current.getStatus() == Status.STARTED || SportingEvent.inPractice(player))
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
        SportingEvent.leavePractice(p);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @EventHandler
    public void onSpray(@NotNull PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR)
            return;
        if (e.getItem() != null && ItemUtil.equals(e.getItem(), ItemUtil.SPRAY)) {
            e.setCancelled(true);
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
            }.runTaskLater(this, 30L);
        }
    }

    @EventHandler
    public void onChangeSpray(@NotNull PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR)
            return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (ItemUtil.equals(item, ItemUtil.SPRAY) && e.getRightClicked() instanceof ItemFrame frame) {
            if (!frame.getPersistentDataContainer().has(GRAFFITI))
                return;
            e.setCancelled(true);
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
            }.runTaskLater(this, 30L);
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
                        p.spawnParticle(Particle.BLOCK, p.getLocation(), 20, 0.2, 0.5, 0.2,
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
            }.runTaskTimer(this, 0L, 10L);
        }
    }
}
