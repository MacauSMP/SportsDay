package org.macausmp.sportsday;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.event.EventStatus;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.gui.admin.PlayerProfileGUI;
import org.macausmp.sportsday.gui.admin.PlayersListGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.event.EventGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.training.SportsTrainingHandler;
import org.macausmp.sportsday.util.FileStorage;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;
import org.macausmp.sportsday.util.TextUtil;
import org.macausmp.sportsday.venue.Track;
import org.macausmp.sportsday.venue.Venue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    private static FileStorage CONTESTANTS_DATA;
    private static FileStorage VENUES_DATA;
    private static FileStorage EVENTS_DATA;
    private static BossBar BOSSBAR;
    public static Team CONTESTANTS;
    public static Team REFEREES;
    public static Team AUDIENCES;
    private static final Set<UUID> EASTER_EGG = new HashSet<>();
    private static final Map<UUID, ContestantData> CONTESTANTS_MAP = new HashMap<>();
    private static final Set<Integer> REGISTERED_NUMBER_LIST = new HashSet<>();
    private static int NUMBER = 1;
    private static SportingEvent CURRENT_EVENT;

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
        SportsRegistry.init();
        CONTESTANTS_DATA = new FileStorage("contestants.dat");
        VENUES_DATA = new FileStorage("venues.dat");
        EVENTS_DATA = new FileStorage("events.dat");
        CONTESTANTS_DATA.read();
        PersistentDataContainer contestants = CONTESTANTS_DATA.getPersistentDataContainer();
        for (NamespacedKey key : contestants.getKeys()) {
            ContestantData data = Objects.requireNonNull(contestants.get(key, ContestantData.CONTESTANT_DATA_TYPE));
            CONTESTANTS_MAP.put(data.getUUID(), data);
        }
        VENUES_DATA.read();
        PersistentDataContainer venues = VENUES_DATA.getPersistentDataContainer();
        SportsRegistry.SPORT.forEach(sport -> sport.loadVenues(venues));
        EVENTS_DATA.read();
        BOSSBAR = BossBar.bossBar(Component.translatable("bossbar.title"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        CONTESTANTS = registerTeam("contestants", "role.contestants", NamedTextColor.GREEN);
        REFEREES = registerTeam("referees", "role.referees", NamedTextColor.GOLD);
        AUDIENCES = registerTeam("audiences", "role.audiences", NamedTextColor.GRAY);
        new CommandManager().register();
        getServer().getPluginManager().registerEvents(this, this);
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

    private @NotNull Team registerTeam(String name, String display, NamedTextColor color) {
        Scoreboard scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
            Component component = Component.translatable(display);
            team.displayName(TextUtil.text(component));
            team.color(color);
            team.prefix(TextUtil.text(Component.translatable("[%s]").arguments(component)));
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
                    if (isContestant(p)) {
                        ContestantData data = getContestant(p.getUniqueId());
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
                SportingEvent event = getCurrentEvent();
                Component competition = Component.newline().append(event == null
                        ? Component.translatable("tablist.idle")
                        : Component.translatable("tablist.current").arguments(event, event.getStatus()));
                Component count = Component.newline().append(Component.translatable("tablist.contestants_count")
                        .arguments(Component.text(getOnlineContestants().size()),
                                Component.text(getContestants().size())));
                return Component.translatable("tablist.title").append(competition).append(count);
            }
        }.runTaskTimer(instance, d, 20L);
    }

    @Override
    public void onDisable() {
        if (getCurrentEvent() != null) {
            CommandSender sender = getServer().getConsoleSender();
            saveEvent(sender);
            terminate(sender);
            getServer().getOnlinePlayers().forEach(p -> {
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
            });
        }
        CONTESTANTS_MAP.forEach((uuid, data) -> CONTESTANTS_DATA.getPersistentDataContainer()
                .set(new NamespacedKey(this, uuid.toString()), ContestantData.CONTESTANT_DATA_TYPE, data));
        CONTESTANTS_DATA.write();
        SportsRegistry.SPORT.forEach(s -> s.saveVenues(VENUES_DATA.getPersistentDataContainer()));
        VENUES_DATA.write();
    }

    /**
     * Start an event.
     *
     * @param sender who host the event
     * @param sport sport of event
     * @param venue venue of event
     * @return {@code True} if event successfully started
     */
    public static boolean startEvent(@NotNull CommandSender sender, @NotNull Sport sport, @NotNull Venue venue) {
        if (CURRENT_EVENT != null) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return false;
        }
        if (!sport.getSetting(Sport.Settings.ENABLE)) {
            sender.sendMessage(Component.translatable("command.competition.disabled").color(NamedTextColor.RED));
            return false;
        }
        int i = sport.getSetting(Sport.Settings.LEAST_PLAYERS_REQUIRED);
        if (getOnlineContestants().size() < i) {
            sender.sendMessage(Component.translatable("command.competition.not_enough_player_required")
                    .arguments(Component.text(i)).color(NamedTextColor.RED));
            return false;
        }
        int j, k;
        if (venue instanceof Track track && (j = track.getStartPoints().size()) < (k = getOnlineContestants().size())) {
            sender.sendMessage(Component.translatable("command.competition.not_enough_start_points")
                    .arguments(Component.text(j), Component.text(k - j))
                    .color(NamedTextColor.RED));
            return false;
        }
        if (getOnlineContestants().size() < getContestants().size()) {
            StringJoiner joiner = new StringJoiner(", ");
            CONTESTANTS_MAP.values().stream()
                    .filter(data -> !data.isOnline())
                    .forEach(data -> joiner.add(data.getName()));
            sender.sendMessage(Component.translatable("command.competition.not_all_online")
                    .arguments(Component.text(joiner.toString())).color(NamedTextColor.RED));
        }
        SportingEvent event = sport.createEvent(venue);
        event.start();
        setCurrentEvent(event);
        sender.sendMessage(Component.translatable("command.competition.start.success").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Save event data into the events.dat file.
     */
    public static void saveEvent(@NotNull CommandSender sender) {
        SportingEvent event = getCurrentEvent();
        if (event == null || event.getStatus() == EventStatus.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.invalid_status")
                    .color(NamedTextColor.RED));
            return;
        }
        PersistentDataContainer pdc = EVENTS_DATA.getPersistentDataContainer();
        PersistentDataContainer saves = pdc.getAdapterContext().newPersistentDataContainer();
        event.save(saves);
        pdc.set(new NamespacedKey(getInstance(), String.valueOf(event.getStartTime())), PersistentDataType.TAG_CONTAINER, saves);
        EVENTS_DATA.write();
        sender.sendMessage(Component.translatable("command.competition.save.success").color(NamedTextColor.GREEN));
    }

    /**
     * Load last event data from the events.dat file and start the event.
     */
    public static void loadEvent(@NotNull CommandSender sender, @Nullable PersistentDataContainer save) {
        if (CURRENT_EVENT != null) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return;
        }
        if (save == null) {
            EVENTS_DATA.read();
            PersistentDataContainer pdc = EVENTS_DATA.getPersistentDataContainer();
            Optional<NamespacedKey> first = pdc.getKeys().stream().findFirst();
            if (first.isEmpty()) {
                sender.sendMessage(Component.translatable("command.competition.load.failed").color(NamedTextColor.RED));
                return;
            }
            save = Objects.requireNonNull(pdc.get(first.get(), PersistentDataType.TAG_CONTAINER));
        }
        Sport sport = SportsRegistry.SPORT.get(Objects.requireNonNull(save
                .get(new NamespacedKey(getInstance(), "sport"), KeyDataType.KEY_DATA_TYPE)));
        if (sport == null) {
            sender.sendMessage(Component.translatable("command.competition.load_unknown").color(NamedTextColor.RED));
            return;
        }
        SportingEvent event = sport.loadEvent(save);
        event.start();
        setCurrentEvent(event);
        sender.sendMessage(Component.translatable("command.competition.load.success").color(NamedTextColor.GREEN));
    }

    public static @NotNull List<PersistentDataContainer> getSavedEvents() {
        EVENTS_DATA.read();
        List<PersistentDataContainer> list = new ArrayList<>();
        PersistentDataContainer pdc = EVENTS_DATA.getPersistentDataContainer();
        for (NamespacedKey key : pdc.getKeys()) {
            PersistentDataContainer save = Objects.requireNonNull(pdc.get(key, PersistentDataType.TAG_CONTAINER));
            save.set(new NamespacedKey(getInstance(), "time"), KeyDataType.KEY_DATA_TYPE, key);
            list.add(save);
        }
        return list;
    }

    public static void clearSavedEvents() {
        EVENTS_DATA.clear();
        EVENTS_DATA.write();
    }

    /**
     * Terminate current competition.
     *
     * @param sender who terminate the competition
     */
    public static void terminate(@NotNull CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == EventStatus.CLOSED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return;
        }
        getCurrentEvent().terminate(sender);
    }

    /**
     * Pause current competition.
     *
     * @param sender who pause the competition
     */
    public static void pause(@NotNull CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == EventStatus.CLOSED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return;
        }
        if (getCurrentEvent().getStatus() == EventStatus.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            return;
        }
        getCurrentEvent().pause(sender);
    }

    /**
     * Unpause current competition.
     *
     * @param sender who unpause the competition
     */
    public static void unpause(@NotNull CommandSender sender) {
        if (getCurrentEvent() == null || getCurrentEvent().getStatus() == EventStatus.CLOSED) {
            sender.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            return;
        }
        if (getCurrentEvent().getStatus() == EventStatus.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            return;
        }
        getCurrentEvent().unpause(sender);
    }

    /**
     * Get the current event.
     *
     * @return current event
     */
    public static @Nullable SportingEvent getCurrentEvent() {
        return CURRENT_EVENT;
    }

    /**
     * Set the current event.
     *
     * @param event new event
     */
    public static void setCurrentEvent(@Nullable SportingEvent event) {
        CURRENT_EVENT = event;
    }

    /**
     * Add a player to contestants list.
     *
     * @param player player to add to the contestants list
     * @param number player's entry number
     * @return {@code True} if player successfully added to the contestants list
     */
    public static boolean join(@NotNull Player player, int number) {
        UUID uuid = player.getUniqueId();
        if (CONTESTANTS_MAP.containsKey(uuid))
            return false;
        CONTESTANTS_MAP.put(uuid, new ContestantData(uuid, number));
        EventGUI.updateAll(EventGUI.class);
        PlayersListGUI.updateAll(PlayersListGUI.class);
        PlayerProfileGUI.updateAll(player.getUniqueId());
        player.sendMessage(Component.translatable("contestant.register.success")
                .arguments(Component.text(number)).color(NamedTextColor.GREEN));
        CONTESTANTS.addPlayer(player);
        return true;
    }

    /**
     * Remove a specific player from contestants list.
     *
     * @param player player to remove from the contestants list
     * @return {@code True} if player successfully removed from the contestants list
     */
    public static boolean leave(@NotNull OfflinePlayer player) {
        if (!isContestant(player))
            return false;
        UUID uuid = player.getUniqueId();
        ContestantData data = getContestant(uuid);
        data.remove();
        if (player.isOnline()) {
            if (getCurrentEvent() != null)
                getCurrentEvent().onLeave(data);
            Objects.requireNonNull(player.getPlayer())
                    .sendMessage(Component.translatable("contestant.unregister.success"));
        }
        REGISTERED_NUMBER_LIST.remove(data.getNumber());
        CONTESTANTS_MAP.remove(uuid);
        EventGUI.updateAll(EventGUI.class);
        PlayersListGUI.updateAll(PlayersListGUI.class);
        PlayerProfileGUI.updateAll(player.getUniqueId());
        AUDIENCES.addPlayer(player);
        return true;
    }

    /**
     * Generate unoccupied contestant numbers.
     *
     * @return unoccupied contestant number
     */
    public static int genNumber() {
        CONTESTANTS_MAP.values().forEach(data -> REGISTERED_NUMBER_LIST.add(data.getNumber()));
        while (REGISTERED_NUMBER_LIST.contains(NUMBER))
            NUMBER++;
        REGISTERED_NUMBER_LIST.add(NUMBER);
        return NUMBER;
    }

    /**
     * Gets a view of all registered contestants.
     *
     * @return a view of registered contestants
     */
    public static @NotNull @Unmodifiable Collection<ContestantData> getContestants() {
        return List.copyOf(CONTESTANTS_MAP.values());
    }

    /**
     * Gets a view of all currently logged in registered contestants.
     *
     * @return a view of currently online registered contestants
     */
    public static @NotNull Collection<ContestantData> getOnlineContestants() {
        return CONTESTANTS_MAP.values().stream().filter(PlayerHolder::isOnline).collect(Collectors.toSet());
    }

    /**
     * Checks if this player is in contestants list.
     *
     * @param player specified player
     * @return {@code True} if the player is in contestants list
     */
    public static boolean isContestant(@NotNull OfflinePlayer player) {
        return CONTESTANTS_MAP.containsKey(player.getUniqueId());
    }

    /**
     * Get {@link ContestantData} by uuid.
     *
     * <p>Plugins should check that {@link #isContestant(OfflinePlayer)} returns {@code True} before calling this method.</p>
     *
     * @param uuid player uuid
     * @return {@link ContestantData} of uuid
     */
    public static @NotNull ContestantData getContestant(@NotNull UUID uuid) {
        ContestantData data = CONTESTANTS_MAP.get(uuid);
        if (data == null)
            throw new IllegalArgumentException("Contestants data collection does not contain this data");
        return data;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BOSSBAR.addViewer(p);
        if (!p.hasPlayedBefore() || getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(p) == null) {
            AUDIENCES.addPlayer(p);
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            p.setGameMode(GameMode.ADVENTURE);
        }
        SportingEvent curr = getCurrentEvent();
        if (curr == null) {
            p.getInventory().setItem(0, ItemUtil.MENU);
            p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        }
        if (p.getPersistentDataContainer().has(SportingEvent.LAST_EVENT_TIME)) {
            long last = Objects.requireNonNull(p.getPersistentDataContainer().get(SportingEvent.LAST_EVENT_TIME, PersistentDataType.LONG));
            if (curr == null || last != curr.getStartTime()) {
                if (p.isInsideVehicle())
                    Objects.requireNonNull(p.getVehicle()).remove();
                p.clearActivePotionEffects();
                p.setFireTicks(0);
                p.setFreezeTicks(0);
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
                p.setRespawnLocation(p.getWorld().getSpawnLocation(), true);
                p.teleport(p.getWorld().getSpawnLocation());
                p.setGameMode(GameMode.ADVENTURE);
                p.getPersistentDataContainer().remove(SportingEvent.LAST_EVENT_TIME);
            }
        }
        if (!isContestant(p))
            return;
        EventGUI.updateAll(EventGUI.class);
        PlayersListGUI.updateAll(PlayersListGUI.class);
        PlayerProfileGUI.updateAll(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        SportsTrainingHandler.leaveSportsTraining(p.getUniqueId());
        if (!isContestant(p))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                EventGUI.updateAll(EventGUI.class);
                PlayersListGUI.updateAll(PlayersListGUI.class);
                PlayerProfileGUI.updateAll(p.getUniqueId());
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
            SportingEvent current = getCurrentEvent();
            if (current != null && current.getStatus() == EventStatus.PROCESSING
                    || SportsTrainingHandler.getTrainingSport(player.getUniqueId()) != null)
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
            new MenuGUI().open(p);
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.CUSTOMIZE)) {
            new CustomizeMenuGUI(p).open(p);
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.OP_BOOK)) {
            if (p.isOp()) {
                new AdminMenuGUI().open(p);
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
                    ++i;
                }
            }.runTaskTimer(this, 0L, 10L);
        }
    }
}
