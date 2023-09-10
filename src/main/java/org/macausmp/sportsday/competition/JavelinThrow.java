package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.CustomizeParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.PlayerHolder;
import org.macausmp.sportsday.util.TextUtil;

import java.util.*;

public class JavelinThrow extends AbstractEvent implements IFieldEvent {
    private final List<ScoreResult> leaderboard = new ArrayList<>();
    private final List<CompetitorData> queue = new ArrayList<>();
    private final Map<UUID, ScoreResult> resultMap = new HashMap<>();
    private CompetitorData currentPlayer = null;
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
        queue.addAll(getCompetitors());
        currentPlayer = null;
        Component c = Component.translatable("event.javelin.order");
        for (int i = 0; i < queue.size();) {
            CompetitorData data = queue.get(i++);
            c = c.appendNewline().append(Component.translatable("event.javelin.queue").args(Component.text(i), Component.text(data.getName())));
        }
        Bukkit.broadcast(c);
    }

    @Override
    public void onStart() {
        Competitions.getOnlineCompetitors().forEach(d -> d.getPlayer().getInventory().setItem(0, TRIDENT));
        onRoundStart();
    }

    private static @NotNull ItemStack trident() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.sportsday.javelin")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("enchantment.sportsday.range").args(Component.translatable("enchantment.level.5")).color(NamedTextColor.GRAY)));
            meta.lore(lore);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            trident.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        });
        return trident;
    }

    @Override
    public void onEnd(boolean force) {
        getLocation().getWorld().getEntitiesByClass(Trident.class).forEach(Trident::remove);
        if (force) return;
        leaderboard.sort((r1, r2) -> Double.compare(r2.getDistance(), r1.getDistance()));
        Component c = Component.text().build();
        for (int i = 0; i < leaderboard.size();) {
            ScoreResult result = leaderboard.get(i++);
            c = c.append(Component.translatable("event.javelin.rank").args(Component.text(i), Component.text(Competitions.getCompetitor(result.uuid).getName()), Component.text(result.getDistance())));
            if (i < leaderboard.size()) c = c.appendNewline();
            if (i <= 3) Competitions.getCompetitor(result.uuid).addScore(4 - i);
            Competitions.getCompetitor(result.uuid).addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @Override
    protected void onPractice(@NotNull Player p) {
        p.getInventory().setItem(0, TRIDENT);
    }

    @EventHandler
    public void onThrow(@NotNull ProjectileLaunchEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            boolean b = event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p);
            if (b || inPractice(p)) {
                resultMap.put(p.getUniqueId(), new ScoreResult(p.getUniqueId(), p.getLocation()));
                trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                trident.setCustomNameVisible(true);
                trident.customName(Component.translatable("event.javelin.javelin_name").args(p.displayName(), Component.text()));
                addRunnable(new BukkitRunnable() {
                    private final CustomizeParticleEffect effect = PlayerCustomize.getProjectileTrail(p);

                    @Override
                    public void run() {
                        if (effect == null || trident.isDead() || trident.isOnGround()) {
                            cancel();
                            return;
                        }
                        p.spawnParticle(effect.getParticle(), trident.getLocation(), 1, 0.3f, 0.3f, 0.3f, effect.getData());
                    }
                }.runTaskTimer(PLUGIN, 0, 1L));
            }
        }
    }

    @EventHandler
    public void onArrived(@NotNull ProjectileHitEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
                ScoreResult result = resultMap.get(p.getUniqueId());
                resultMap.remove(p.getUniqueId());
                if (result == null) return;
                result.setTridentLocation(trident);
                leaderboard.add(result);
                trident.customName(Component.translatable("event.javelin.javelin_name").args(p.displayName(), Component.text(result.getDistance())));
                getWorld().strikeLightningEffect(trident.getLocation());
                Bukkit.broadcast(Component.translatable("event.javelin.result").args(p.displayName(), Component.text(result.getDistance())));
                onRoundEnd();
            } else {
                ScoreResult result = resultMap.get(p.getUniqueId());
                resultMap.remove(p.getUniqueId());
                if (result == null) return;
                result.setTridentLocation(trident);
                trident.remove();
                if (p.isOnline()) p.sendMessage(Component.translatable("event.javelin.practice_result").args(Component.text(result.getDistance())));
                if (inPractice(p)) p.getInventory().setItem(0, TRIDENT);
            }
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
            if (resultMap.containsKey(p.getUniqueId())) return;
            if (currentPlayer != null && p.getUniqueId().equals(currentPlayer.getUUID())) {
                reconnectTask.cancel();
                p.getInventory().setItem(0, TRIDENT);
                Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_reconnected").color(NamedTextColor.YELLOW));
                p.teleport(getLocation());
                p.setGameMode(GameMode.ADVENTURE);
                queue.remove(Competitions.getCompetitor(p.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
            if (resultMap.containsKey(p.getUniqueId())) return;
            if (currentPlayer != null && p.getUniqueId().equals(currentPlayer.getUUID())) {
                p.getInventory().clear();
                reconnectTask = addRunnable(new BukkitRunnable() {
                    int i = PLUGIN.getConfig().getInt("reconnect_time");
                    @Override
                    public void run() {
                        if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected").args(Component.text(i)).color(NamedTextColor.YELLOW));
                        if (i-- == 0) {
                            onRoundEnd();
                            this.cancel();
                        }
                    }
                }.runTaskTimer(PLUGIN, 0L, 20L));
            }
        }
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public List<ScoreResult> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void onRoundStart() {
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
        for (CompetitorData d : queue) {
            currentPlayer = d;
            if (d.getOfflinePlayer().isOnline()) {
                currentPlayer.getPlayer().teleport(getLocation());
                currentPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
                queue.remove(d);
                return;
            }
        }
        if (reconnectTask == null || reconnectTask.isCancelled()) {
            reconnectTask = addRunnable(new BukkitRunnable() {
                int i = PLUGIN.getConfig().getInt("reconnect_time");
                @Override
                public void run() {
                    if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        queue.remove(currentPlayer);
                        onRoundEnd();
                        this.cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    @Override
    public void onRoundEnd() {
        currentPlayer = null;
        if (!queue.isEmpty()) {
            nextRound();
        } else {
            end(false);
        }
    }

    @Override
    public void nextRound() {
        addRunnable(new BukkitRunnable() {
            int i = 3;
            @Override
            public void run() {
                if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.next_round_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
                    onRoundStart();
                    this.cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private static class ScoreResult implements PlayerHolder {
        private final UUID uuid;
        private final Location loc;
        private double distance;

        public ScoreResult(UUID uuid, Location loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        public final void setTridentLocation(@NotNull Trident trident) {
            this.distance = loc.distance(trident.getLocation());
        }

        public final double getDistance() {
            return this.distance;
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }
    }
}
