package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
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
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.event.JavelinGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerHolder;

import java.util.*;

public class JavelinThrow extends AbstractEvent implements IFieldEvent, Savable {
    private final List<ContestantData> queue = new ArrayList<>();
    private final Map<UUID, ScoreResult> resultMap = new HashMap<>();
    private ContestantData currentPlayer = null;
    private BukkitTask reconnectTask;
    private static final ItemStack TRIDENT = trident();

    public JavelinThrow() {
        super("javelin_throw");
    }

    @Override
    public void onSetup() {
        getLocation().getWorld().getEntitiesByClass(Trident.class).forEach(Trident::remove);
        resultMap.clear();
        queue.clear();
        queue.addAll(getContestants());
        currentPlayer = null;
        TranslatableComponent.Builder builder = Component.translatable("event.javelin.order").toBuilder();
        for (int i = 0; i < queue.size();) {
            ContestantData data = queue.get(i);
            builder.appendNewline().append(Component.translatable("event.javelin.queue")
                    .arguments(Component.text(++i), Component.text(data.getName())));
        }
        Bukkit.broadcast(builder.build());
    }

    @Override
    public void onStart() {
        nextMatch();
    }

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

    @Override
    public void onEnd(boolean force) {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().getInventory().remove(TRIDENT));
        getLocation().getWorld().getEntitiesByClass(Trident.class).forEach(Trident::remove);
        if (force)
            return;
        List<ScoreResult> results = resultMap.values().stream().sorted().toList();
        getLeaderboard().addAll(results.stream().map(r -> Competitions.getContestant(r.uuid)).toList());
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

    @EventHandler
    public void onThrow(@NotNull ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (check(p) || inPractice(p, this)) {
                resultMap.put(p.getUniqueId(), new ScoreResult(p.getUniqueId(), p.getLocation()));
                trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                trident.setCustomNameVisible(true);
                trident.customName(Component.translatable("event.javelin.javelin_name")
                        .arguments(p.displayName(), Component.text()));
                final ParticleEffect effect = PlayerCustomize.getProjectileTrail(p);
                if (effect != null) {
                    addRunnable(new BukkitRunnable() {
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
    }

    @EventHandler
    public void onArrived(@NotNull ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            UUID uuid = p.getUniqueId();
            if (resultMap.containsKey(uuid)) {
                ScoreResult result = resultMap.get(uuid);
                if (result == null)
                    return;
                result.setTridentLocation(trident);
                if (check(p) && currentPlayer.getUUID().equals(uuid)) {
                    trident.customName(Component.translatable("event.javelin.javelin_name")
                            .arguments(p.displayName(), Component.text(result.distance)));
                    getWorld().strikeLightningEffect(trident.getLocation());
                    Bukkit.broadcast(Component.translatable("event.javelin.result")
                            .arguments(p.displayName(), Component.text(result.distance)));
                    onMatchEnd();
                } else {
                    resultMap.remove(uuid);
                    trident.remove();
                    if (p.isOnline())
                        p.sendMessage(Component.translatable("event.javelin.practice_result")
                                .arguments(Component.text(result.distance)));
                    if (inPractice(p, this))
                        p.getInventory().setItem(0, TRIDENT);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (check(p)) {
            if (resultMap.containsKey(p.getUniqueId()))
                return;
            if (currentPlayer != null && currentPlayer.getUUID().equals(p.getUniqueId())) {
                reconnectTask.cancel();
                p.getInventory().setItem(0, TRIDENT);
                Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_reconnected").color(NamedTextColor.YELLOW));
                p.teleportAsync(getLocation());
                p.setGameMode(GameMode.ADVENTURE);
                queue.remove(Competitions.getContestant(p.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (check(p)) {
            if (resultMap.containsKey(p.getUniqueId()))
                return;
            if (currentPlayer != null && currentPlayer.getUUID().equals(p.getUniqueId())) {
                p.getInventory().clear();
                reconnectTask = addRunnable(new BukkitRunnable() {
                    int i = PLUGIN.getConfig().getInt("reconnect_time");
                    @Override
                    public void run() {
                        if (i > 0)
                            Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected")
                                    .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                        if (i-- == 0) {
                            onMatchEnd();
                            cancel();
                        }
                    }
                }.runTaskTimer(PLUGIN, 0L, 20L));
            }
        }
    }

    private boolean check(Player p) {
        return Competitions.getCurrentEvent() == this && getStatus() == Status.STARTED && Competitions.isContestant(p);
    }

    @Override
    public void onDisqualification(@NotNull ContestantData contestant) {
        super.onDisqualification(contestant);
        queue.remove(contestant);
        if (currentPlayer != null && currentPlayer.getUUID().equals(contestant.getUUID()))
            onMatchEnd();
    }

    @Override
    protected void onPractice(@NotNull Player player) {
        player.getInventory().setItem(0, TRIDENT);
    }

    @Override
    public void onMatchStart() {
        if (currentPlayer.isOnline()) {
            currentPlayer.getPlayer().teleport(getLocation());
            currentPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
            currentPlayer.getPlayer().getInventory().setItem(0, TRIDENT);
            return;
        }
        if (reconnectTask == null || reconnectTask.isCancelled()) {
            reconnectTask = addRunnable(new BukkitRunnable() {
                int i = PLUGIN.getConfig().getInt("reconnect_time");
                @Override
                public void run() {
                    if (i > 0)
                        Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected")
                                .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        queue.remove(currentPlayer);
                        onMatchEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    @Override
    public void onMatchEnd() {
        currentPlayer = null;
        if (!queue.isEmpty())
            nextMatch();
        else
            end(false);
        JavelinGUI.updateGUI();
    }

    @Override
    public void nextMatch() {
        addRunnable(new BukkitRunnable() {
            int i = 3;
            @Override
            public void run() {
                if (isPaused()) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.pause.broadcast"));
                    return;
                }
                if (i > 0)
                    Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.next_round_countdown")
                            .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
                    Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
                    for (ContestantData d : queue) {
                        currentPlayer = d;
                        if (currentPlayer.isOnline()) {
                            queue.remove(d);
                            break;
                        }
                    }
                    onMatchStart();
                    JavelinGUI.updateGUI();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    public ContestantData getCurrentPlayer() {
        return currentPlayer;
    }

    public ScoreResult getScoreResult(UUID uuid) {
        return resultMap.get(uuid);
    }

    @Override
    public void load(@NotNull PersistentDataContainer data) {
        init();
        queue.clear();
        resultMap.clear();
        currentPlayer = null;
        if (data.has(new NamespacedKey(PLUGIN, "current_player"))) {
            String current = Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "current_player"), PersistentDataType.STRING));
            queue.add(Competitions.getContestant(UUID.fromString(current)));
        }
        Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "queue"),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING)))
                .forEach(uuid -> queue.add(Competitions.getContestant(UUID.fromString(uuid))));
        Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "result"),
                PersistentDataType.LIST.listTypeFrom(new ScoreResultDataType())))
                .forEach(sr -> resultMap.put(sr.uuid, sr));
        TranslatableComponent.Builder builder = Component.translatable("event.javelin.order").toBuilder();
        for (int i = 0; i < queue.size();) {
            ContestantData d = queue.get(i);
            builder.appendNewline().append(Component.translatable("event.javelin.queue")
                    .arguments(Component.text(++i), Component.text(d.getName())));
        }
        Bukkit.broadcast(builder.build());
        start();
    }

    @Override
    public void save(@NotNull PersistentDataContainer data) {
        if (currentPlayer != null)
            data.set(new NamespacedKey(PLUGIN, "current_player"), PersistentDataType.STRING,
                    currentPlayer.getUUID().toString());
        data.set(new NamespacedKey(PLUGIN, "queue"),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                queue.stream().map(d -> d.getUUID().toString()).toList());
        data.set(new NamespacedKey(PLUGIN, "result"),
                PersistentDataType.LIST.listTypeFrom(new ScoreResultDataType()),
                resultMap.values().stream().filter(ScoreResult::isSet).toList());
    }

    public static final class ScoreResult implements PlayerHolder, Comparable<ScoreResult> {
        private final UUID uuid;
        private final Location loc;
        private double distance = -1;

        private ScoreResult(@NotNull UUID uuid, @NotNull Location loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        private void setTridentLocation(@NotNull Trident trident) {
            distance = loc.distance(trident.getLocation());
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
        public int compareTo(@NotNull JavelinThrow.ScoreResult o) {
            return Double.compare(o.distance, this.distance);
        }
    }

    private final class ScoreResultDataType implements PersistentDataType<PersistentDataContainer, ScoreResult> {
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
            pdc.set(new NamespacedKey(PLUGIN, "x"), DOUBLE, complex.loc.x());
            pdc.set(new NamespacedKey(PLUGIN, "y"), DOUBLE, complex.loc.y());
            pdc.set(new NamespacedKey(PLUGIN, "z"), DOUBLE, complex.loc.z());
            pdc.set(new NamespacedKey(PLUGIN, "d"), DOUBLE, complex.distance);
            return pdc;
        }

        @Override
        public @NotNull ScoreResult fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            double x = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "x"), DOUBLE));
            double y = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "y"), DOUBLE));
            double z = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "z"), DOUBLE));
            ScoreResult sr = new ScoreResult(uuid, new Location(getWorld(), x, y, z));
            sr.distance = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "d"), DOUBLE));
            return sr;
        }
    }
}
