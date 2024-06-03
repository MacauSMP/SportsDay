package org.macausmp.sportsday;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.CompetitionConsoleGUI;
import org.macausmp.sportsday.gui.competition.ContestantProfileGUI;
import org.macausmp.sportsday.gui.competition.ContestantsListGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    private CommandManager commandManager;
    public static Team CONTESTANTS;
    public static Team REFEREES;
    public static Team AUDIENCES;
    private static BossBar BOSSBAR;

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
        getConfig().options().copyDefaults(true);
        saveConfig();
        Competitions.load();
        CONTESTANTS = registerTeam("contestants", Component.translatable("role.contestants"), NamedTextColor.GREEN);
        REFEREES = registerTeam("referees", Component.translatable("role.referees"), NamedTextColor.GOLD);
        AUDIENCES = registerTeam("audiences", Component.translatable("role.audiences"), NamedTextColor.GRAY);
        if (BOSSBAR == null)
            BOSSBAR = BossBar.bossBar(Component.translatable("bossbar.title"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        registerCommand();
        registerListener();
        setGameRules();
        setTabList();
    }

    private void registerTranslation() {
        TranslationRegistry registry = TranslationRegistry.create(Key.key("sportsday:resource"));
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

    private void registerCommand() {
        if (commandManager == null)
            commandManager = new CommandManager();
        commandManager.register();
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new SportsDayListener(), this);
        Competitions.EVENTS.values().forEach(e -> getServer().getPluginManager().registerEvents(e, this));
    }

    private void setGameRules() {
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
    }

    @Override
    public void onDisable() {
        IEvent event = Competitions.getCurrentEvent();
        if (event != null) {
            CommandSender sender = getServer().getConsoleSender();
            if (event instanceof Savable)
                Competitions.saveEventData(sender);
            Competitions.forceEnd(sender);
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

    private void setTabList() {
        long d = Math.round(20f - Instant.now().getNano() / 50000000f);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        new BukkitRunnable() {
            @Override
            public void run() {
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
                        IEvent event = Competitions.getCurrentEvent();
                        Component competition = Component.newline().append(event == null
                                ? Component.translatable("tablist.idle")
                                : Component.translatable("tablist.current").arguments(event.getName(), event.getStatus().getName()));
                        Component count = Component.newline().append(Component.translatable("tablist.contestants_count")
                                .arguments(Component.text(Competitions.getOnlineContestants().size()),
                                        Component.text(Competitions.getContestants().size())));
                        return Component.translatable("tablist.title").append(competition).append(count);
                    }
                }.runTaskTimer(instance, 0L, 20L);
            }
        }.runTaskLater(this, d);
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
        IEvent curr = Competitions.getCurrentEvent();
        if (curr == null) {
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        }
        if (p.getPersistentDataContainer().has(AbstractEvent.IN_GAME)) {
            long last = Objects.requireNonNull(p.getPersistentDataContainer().get(AbstractEvent.IN_GAME, PersistentDataType.LONG));
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
                p.getPersistentDataContainer().remove(AbstractEvent.IN_GAME);
            }
        }
        if (!Competitions.isContestant(p))
            return;
        CompetitionConsoleGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        ContestantProfileGUI.updateProfile(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        AbstractEvent.leavePractice(p);
        if (!Competitions.isContestant(p))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                CompetitionConsoleGUI.updateGUI();
                ContestantsListGUI.updateGUI();
                ContestantProfileGUI.updateProfile(p.getUniqueId());
            }
        }.runTaskLater(this, 1L);
    }
}
