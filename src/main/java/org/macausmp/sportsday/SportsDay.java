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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.ScoreboardHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    public static Team COMPETITOR;
    public static Team REFEREE;
    public static Team AUDIENCE;
    static BossBar BOSSBAR;
    private ConfigManager configManager;
    private CommandManager commandManager;
    ScoreboardHandler scoreboardHandler;

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
        if (scoreboardHandler == null) scoreboardHandler = new ScoreboardHandler();
        getServer().getOnlinePlayers().forEach(scoreboardHandler::setScoreboard);
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
        getServer().getPluginManager().registerEvents(Competitions.ELYTRA_RACING, this);
        getServer().getPluginManager().registerEvents(Competitions.ICE_BOAT_RACING, this);
        getServer().getPluginManager().registerEvents(Competitions.JAVELIN_THROW, this);
        getServer().getPluginManager().registerEvents(Competitions.OBSTACLE_COURSE, this);
        getServer().getPluginManager().registerEvents(Competitions.PARKOUR, this);
        getServer().getPluginManager().registerEvents(Competitions.SUMO, this);
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
        if (Competitions.getCurrentEvent() != null) Competitions.forceEnd(getServer().getConsoleSender());
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.teleport(p.getWorld().getSpawnLocation());
            p.setGameMode(GameMode.ADVENTURE);
        });
        Competitions.getOnlineCompetitors().forEach(d -> {
            d.getPlayer().getInventory().clear();
            PlayerCustomize.suitUp(d.getPlayer());
            d.getPlayer().getInventory().setItem(0, ItemUtil.MENU);
            d.getPlayer().getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        });
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
                    Component header;
                    Component competition;
                    Component time;
                    Component footer;
                    @Override
                    public void run() {
                        header = Component.translatable("tablist.title").appendNewline();
                        if (Competitions.getCurrentEvent() != null) {
                            Component cn = Competitions.getCurrentEvent().getName();
                            Component sn = Competitions.getCurrentEvent().getStatus().getName();
                            competition = Component.translatable("tablist.current").args(cn, sn);
                        } else {
                            competition = Component.translatable("tablist.idle");
                        }
                        header = header.append(competition);
                        time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        for (Player p : getServer().getOnlinePlayers()) {
                            Component ping = Component.translatable("tablist.ping").color(NamedTextColor.GREEN).args(Component.text(p.getPing()).color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                            footer = Component.translatable("tablist.local_time").args(time).color(NamedTextColor.GREEN).appendNewline().append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());
                        }
                    }
                }.runTaskTimer(instance, 0L, 20L);
            }
        }.runTaskLater(this, d);
    }
}
