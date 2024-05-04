package org.macausmp.sportsday;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
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
import org.macausmp.sportsday.competition.AbstractEvent;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.CompetitorListGUI;
import org.macausmp.sportsday.gui.competition.CompetitorProfileGUI;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    private ConfigManager configManager;
    private CommandManager commandManager;
    public static Team COMPETITOR;
    public static Team REFEREE;
    public static Team AUDIENCE;
    private static BossBar BOSSBAR;

    public static SportsDay getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (configManager == null) configManager = new ConfigManager();
        configManager.setup();
        configManager.saveConfig();
        registerTranslation();
        Competitions.load();
        Scoreboard scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        COMPETITOR = registerTeam(scoreboard, "competitor", Component.translatable("role.competitor"), NamedTextColor.GREEN);
        REFEREE = registerTeam(scoreboard, "referee", Component.translatable("role.referee"), NamedTextColor.GOLD);
        AUDIENCE = registerTeam(scoreboard, "audience", Component.translatable("role.audience"), NamedTextColor.GRAY);
        BOSSBAR = BossBar.bossBar(Component.translatable("bossbar.title"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        setGameRules();
        registerCommand();
        registerListener();
        setTabList();
    }

    private void registerTranslation() {
        TranslationRegistry registry = TranslationRegistry.create(Key.key("sportsday:resource"));
        // There is no way to use an alternative language other than the en_US localization file that ships as part of the vanilla jar
        ResourceBundle bundle = ResourceBundle.getBundle("lang.Bundle", Locale.US, UTF8ResourceBundleControl.get());
        registry.registerAll(Locale.US, bundle, true);
        GlobalTranslator.translator().addSource(registry);
    }

    private Team registerTeam(@NotNull Scoreboard scoreboard, String name, Component display, NamedTextColor color) {
        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.registerNewTeam(name);
            team.displayName(display);
            team.color(color);
            team.prefix(Component.translatable("[%s]").args(display));
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        return scoreboard.getTeam(name);
    }

    private void registerCommand() {
        if (commandManager == null) commandManager = new CommandManager();
        commandManager.register();
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new CompetitionListener(), this);
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
        });
    }

    @Override
    public void onDisable() {
        if (Competitions.getCurrentEvent() != null) {
            Competitions.forceEnd(getServer().getConsoleSender());
            Bukkit.getOnlinePlayers().forEach(p -> {
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

    /**
     * Get the config manager
     * @return config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    private void setTabList() {
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Component header = getHeader();
                        Component time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        for (Player p : getServer().getOnlinePlayers()) {
                            if (Competitions.isCompetitor(p)) {
                                CompetitorData data = Competitions.getCompetitor(p.getUniqueId());
                                Component number = Component.translatable("tablist.number").args(Component.text(data.getNumber()));
                                Component score = Component.translatable("tablist.score").args(Component.text(data.getScore()));
                                header = header.appendNewline().append(number).appendNewline().append(score);
                            }
                            Component ping = Component.translatable("tablist.ping").args(Component.text(p.getPing() + "ms").color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                            Component footer = Component.translatable("tablist.local_time").args(time).appendNewline().append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());
                        }
                    }

                    private @NotNull Component getHeader() {
                        IEvent event = Competitions.getCurrentEvent();
                        Component competition = event == null ? Component.translatable("tablist.idle") : Component.translatable("tablist.current").args(event.getName(), event.getStatus().getName());
                        Component count = Component.translatable("tablist.competitor_count").args(Component.text(Competitions.getOnlineCompetitors().size()), Component.text(getServer().getOnlinePlayers().size()));
                        return Component.translatable("tablist.title").appendNewline().append(competition).appendNewline().append(count);
                    }
                }.runTaskTimer(instance, 0L, 20L);
            }
        }.runTaskLater(this, d);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BOSSBAR.addViewer(p);
        if (!p.hasPlayedBefore()) SportsDay.AUDIENCE.addPlayer(p);
        if (p.getPersistentDataContainer().has(AbstractEvent.IN_GAME)) {
            IEvent curr = Competitions.getCurrentEvent();
            long last = Objects.requireNonNull(p.getPersistentDataContainer().get(AbstractEvent.IN_GAME, PersistentDataType.LONG));
            if (curr == null || last != curr.getLastTime()) {
                if (p.isInsideVehicle()) Objects.requireNonNull(p.getVehicle()).remove();
                p.clearActivePotionEffects();
                p.setFireTicks(0);
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                p.setBedSpawnLocation(p.getWorld().getSpawnLocation(), true);
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
                p.getPersistentDataContainer().remove(AbstractEvent.IN_GAME);
            }
        }
        if (!Competitions.isCompetitor(p)) return;
        CompetitionInfoGUI.updateGUI();
        CompetitorListGUI.updateGUI();
        CompetitorProfileGUI.updateProfile(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        AbstractEvent.leavePractice(p);
        if (!Competitions.isCompetitor(p)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                CompetitionInfoGUI.updateGUI();
                CompetitorListGUI.updateGUI();
                CompetitorProfileGUI.updateProfile(p.getUniqueId());
            }
        }.runTaskLater(this, 1L);
    }
}
