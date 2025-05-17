package org.macausmp.sportsday.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.PlayerHolder;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.event.JavelinThrowEventGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.*;

public class JavelinThrowEvent extends SportingEvent {
    private static final PersistentDataType<PersistentDataContainer, ScoreResult> SCORE_RESULT_DATA_TYPE = new PersistentDataType<>() {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<ScoreResult> getComplexType() {
            return ScoreResult.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull ScoreResult complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "uuid"), STRING, complex.uuid.toString());
            pdc.set(new NamespacedKey(PLUGIN, "x"), DOUBLE, complex.loc.getX());
            pdc.set(new NamespacedKey(PLUGIN, "y"), DOUBLE, complex.loc.getY());
            pdc.set(new NamespacedKey(PLUGIN, "z"), DOUBLE, complex.loc.getZ());
            pdc.set(new NamespacedKey(PLUGIN, "d"), DOUBLE, complex.distance);
            return pdc;
        }

        @Override
        public @NotNull ScoreResult fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            double x = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "x"), DOUBLE));
            double y = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "y"), DOUBLE));
            double z = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "z"), DOUBLE));
            ScoreResult sr = new ScoreResult(uuid, new Vector(x, y, z));
            sr.distance = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "d"), DOUBLE));
            return sr;
        }
    };
    private static final ItemStack TRIDENT = trident();

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

    private final List<ContestantData> queue = new ArrayList<>();
    private final Map<UUID, ScoreResult> resultMap = new HashMap<>();
    private ContestantData currentContestant = null;
    private BukkitTask reconnectTask;

    public JavelinThrowEvent(@NotNull Sport sport, @NotNull Venue venue, @Nullable PersistentDataContainer save) {
        super(sport, venue, save);
        getVenue().getLocation().getWorld().getEntitiesByClass(Trident.class).forEach(Trident::remove);
        TranslatableComponent.Builder builder = Component.translatable("event.javelin.order").toBuilder();
        if (save == null) {
            this.queue.addAll(getContestants());
        } else {
            if (save.has(new NamespacedKey(PLUGIN, "current_player"))) {
                String current = Objects.requireNonNull(save.get(new NamespacedKey(PLUGIN, "current_player"), PersistentDataType.STRING));
                queue.add(SportsDay.getContestant(UUID.fromString(current)));
            }
            Objects.requireNonNull(save.get(new NamespacedKey(PLUGIN, "queue"),
                            PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING)))
                    .forEach(uuid -> queue.add(SportsDay.getContestant(UUID.fromString(uuid))));
            Objects.requireNonNull(save.get(new NamespacedKey(PLUGIN, "result"),
                            PersistentDataType.LIST.listTypeFrom(SCORE_RESULT_DATA_TYPE)))
                    .forEach(sr -> resultMap.put(sr.uuid, sr));
        }
        for (int i = 0; i < this.queue.size();) {
            ContestantData data = this.queue.get(i);
            builder.appendNewline().append(Component.translatable("event.javelin.queue")
                    .arguments(Component.text(++i), Component.text(data.getName())));
        }
        Bukkit.broadcast(builder.build());
    }

    @Override
    public void save(@NotNull PersistentDataContainer data) {
        super.save(data);
        if (currentContestant != null)
            data.set(new NamespacedKey(PLUGIN, "current_player"), PersistentDataType.STRING,
                    currentContestant.getUUID().toString());
        data.set(new NamespacedKey(PLUGIN, "queue"),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                queue.stream().map(d -> d.getUUID().toString()).toList());
        data.set(new NamespacedKey(PLUGIN, "result"),
                PersistentDataType.LIST.listTypeFrom(SCORE_RESULT_DATA_TYPE),
                resultMap.values().stream().filter(ScoreResult::isSet).toList());
    }

    @Override
    public JavelinThrowEventGUI getEventGUI() {
        return new JavelinThrowEventGUI(this);
    }

    public ContestantData getCurrentContestant() {
        return currentContestant;
    }

    public ScoreResult getScoreResult(UUID uuid) {
        return resultMap.get(uuid);
    }

    @Override
    protected void onStart() {
        nextContestant();
    }

    @Override
    protected void onEnd() {
        List<ScoreResult> results = resultMap.values().stream().sorted().toList();
        getLeaderboard().addAll(results.stream().map(r -> SportsDay.getContestant(r.uuid)).toList());
        TranslatableComponent.Builder builder = Component.translatable("event.result").toBuilder();
        for (int i = 0; i < getLeaderboard().size();) {
            ContestantData d = getLeaderboard().get(i);
            ScoreResult r = results.get(i);
            builder.appendNewline().append(Component.translatable("event.javelin.rank")
                    .arguments(Component.text(++i), Component.text(d.getName()), Component.text(r.distance)));
            if (i <= 3)
                d.addScore(4 - i);
            d.addScore(1);
        }
        Bukkit.broadcast(builder.build());
    }

    @Override
    protected void onClose() {
        SportsDay.getOnlineContestants().forEach(d -> d.getPlayer().getInventory().remove(Material.TRIDENT));
        getVenue().getLocation().getWorld().getEntitiesByClass(Trident.class).forEach(Trident::remove);
    }

    @Override
    public void onLeave(ContestantData contestant) {
        queue.remove(contestant);
        resultMap.remove(contestant.getUUID());
        super.onLeave(contestant);
        if (currentContestant != null && currentContestant.getUUID().equals(contestant.getUUID()))
            onThrowingEnd();
    }

    protected void onThrowingStart() {
        if (currentContestant.isOnline()) {
            currentContestant.getPlayer().teleport(getVenue().getLocation());
            currentContestant.getPlayer().setGameMode(GameMode.ADVENTURE);
            currentContestant.getPlayer().getInventory().setItem(0, TRIDENT);
            return;
        }
        if (reconnectTask == null || reconnectTask.isCancelled()) {
            reconnectTask = addTask(new BukkitRunnable() {
                int i = PLUGIN.getConfig().getInt("reconnect_time");

                @Override
                public void run() {
                    if (i > 0)
                        Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected")
                                .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        queue.remove(currentContestant);
                        onThrowingEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    protected void onThrowingEnd() {
        currentContestant = null;
        if (!queue.isEmpty())
            nextContestant();
        else
            end();
        JavelinThrowEventGUI.updateAll(JavelinThrowEventGUI.class);
    }

    protected void nextContestant() {
        addTask(new BukkitRunnable() {
            int i = 3;

            @Override
            public void run() {
                if (isPaused()) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.pause"));
                    return;
                }
                if (i > 0)
                    Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.next_round_countdown")
                            .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
                    Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
                    for (ContestantData d : queue) {
                        currentContestant = d;
                        if (currentContestant.isOnline()) {
                            queue.remove(d);
                            break;
                        }
                    }
                    onThrowingStart();
                    JavelinThrowEventGUI.updateAll(JavelinThrowEventGUI.class);
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    @EventHandler
    public void onThrow(@NotNull ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (!predicate.test(p))
                return;
            resultMap.put(p.getUniqueId(), new ScoreResult(p.getUniqueId(), p.getLocation().toVector()));
            trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            trident.setCustomNameVisible(true);
            trident.customName(Component.translatable("event.javelin.javelin_name")
                    .arguments(p.displayName(), Component.text()));
            final ParticleEffect effect = PlayerCustomize.getProjectileTrail(p);
            if (effect != null) {
                addTask(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (trident.isDead() || trident.isOnGround()) {
                            cancel();
                            return;
                        }
                        effect.play(p, trident.getLocation());
                    }
                }.runTaskTimer(PLUGIN, 0, 1L));
            }
        }
    }

    @EventHandler
    public void onArrived(@NotNull ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            UUID uuid = p.getUniqueId();
            if (!predicate.test(p) || !currentContestant.getUUID().equals(uuid))
                return;
            if (!resultMap.containsKey(uuid))
                return;
            ScoreResult result = resultMap.get(uuid);
            if (result == null)
                return;
            result.setTridentLocation(trident);
            trident.customName(Component.translatable("event.javelin.javelin_name")
                    .arguments(p.displayName(), Component.text(result.distance)));
            getVenue().getLocation().getWorld().strikeLightningEffect(trident.getLocation());
            Bukkit.broadcast(Component.translatable("event.javelin.result")
                    .arguments(p.displayName(), Component.text(result.distance)));
            onThrowingEnd();
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p))
            return;
        if (resultMap.containsKey(p.getUniqueId()))
            return;
        if (currentContestant != null && currentContestant.getUUID().equals(p.getUniqueId())) {
            reconnectTask.cancel();
            p.getInventory().setItem(0, TRIDENT);
            Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_reconnected").color(NamedTextColor.YELLOW));
            p.teleportAsync(getVenue().getLocation());
            p.setGameMode(GameMode.ADVENTURE);
            queue.remove(SportsDay.getContestant(p.getUniqueId()));
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p))
            return;
        if (resultMap.containsKey(p.getUniqueId()))
            return;
        if (currentContestant != null && currentContestant.getUUID().equals(p.getUniqueId())) {
            p.getInventory().clear();
            reconnectTask = addTask(new BukkitRunnable() {
                int i = PLUGIN.getConfig().getInt("reconnect_time");

                @Override
                public void run() {
                    if (i > 0)
                        Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected")
                                .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        onThrowingEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    public static final class ScoreResult implements PlayerHolder, Comparable<ScoreResult> {
        private final UUID uuid;
        private final Vector loc;
        private double distance = -1;

        private ScoreResult(@NotNull UUID uuid, @NotNull Vector loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        private void setTridentLocation(@NotNull Trident trident) {
            distance = loc.distance(trident.getLocation().toVector());
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }

        public double getDistance() {
            return distance;
        }

        public boolean isSet() {
            return distance != -1;
        }

        @Override
        public int compareTo(@NotNull ScoreResult o) {
            return Double.compare(o.distance, this.distance);
        }
    }
}
