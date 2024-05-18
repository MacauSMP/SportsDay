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
import org.macausmp.sportsday.gui.competition.event.JavelinGUI;
import org.macausmp.sportsday.util.ContestantData;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerHolder;

import java.util.*;

public class JavelinThrow extends AbstractEvent implements IFieldEvent {
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
        Component c = Component.translatable("event.javelin.order");
        for (int i = 0; i < queue.size();) {
            ContestantData data = queue.get(i);
            c = c.appendNewline().append(Component.translatable("event.javelin.queue")
                    .args(Component.text(++i), Component.text(data.getName())));
        }
        Bukkit.broadcast(c);
    }

    @Override
    public void onStart() {
        Competitions.getOnlineContestants().forEach(d -> d.getPlayer().getInventory().setItem(0, TRIDENT));
        onMatchStart();
    }

    private static @NotNull ItemStack trident() {
        String display = "item.sportsday.javelin";
        Component lore = Component.translatable("enchantment.sportsday.range")
                .args(Component.translatable("enchantment.level.5")).color(NamedTextColor.GRAY);
        ItemStack trident = ItemUtil.setBind(ItemUtil.item(Material.TRIDENT, null, display, lore));
        trident.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            trident.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
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
        Component c = Component.translatable("event.result");
        for (int i = 0; i < getLeaderboard().size();) {
            ContestantData d = getLeaderboard().get(i);
            ScoreResult r = results.get(i);
            c = c.appendNewline().append(Component.translatable("event.javelin.rank")
                    .args(Component.text(++i), Component.text(d.getName()), Component.text(r.distance)));
            if (i <= 3)
                d.addScore(4 - i);
            d.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @EventHandler
    public void onThrow(@NotNull ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            if (check(p) || inPractice(p, this)) {
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
        if (e.getEntity().getShooter() instanceof Player p && e.getEntity() instanceof Trident trident) {
            UUID uuid = p.getUniqueId();
            if (resultMap.containsKey(uuid)) {
                ScoreResult result = resultMap.get(uuid);
                if (result == null)
                    return;
                result.setTridentLocation(trident);
                if (check(p) && currentPlayer.getUUID().equals(uuid)) {
                    trident.customName(Component.translatable("event.javelin.javelin_name")
                            .args(p.displayName(), Component.text(result.distance)));
                    getWorld().strikeLightningEffect(trident.getLocation());
                    Bukkit.broadcast(Component.translatable("event.javelin.result")
                            .args(p.displayName(), Component.text(result.distance)));
                    onMatchEnd();
                } else {
                    resultMap.remove(uuid);
                    trident.remove();
                    if (p.isOnline())
                        p.sendMessage(Component.translatable("event.javelin.practice_result")
                                .args(Component.text(result.distance)));
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
                p.teleport(getLocation());
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
                                    .args(Component.text(i)).color(NamedTextColor.YELLOW));
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
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
        queue.removeIf(d -> !getContestants().contains(d));
        for (ContestantData d : queue) {
            currentPlayer = d;
            if (currentPlayer.isOnline()) {
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
                    if (i > 0)
                        Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected")
                                .args(Component.text(i)).color(NamedTextColor.YELLOW));
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
                if (i > 0)
                    Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.next_round_countdown")
                            .args(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
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

    public static final class ScoreResult implements PlayerHolder, Comparable<ScoreResult> {
        private final UUID uuid;
        private final Location loc;
        private double distance;

        private ScoreResult(UUID uuid, Location loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        private void setTridentLocation(@NotNull Trident trident) {
            this.distance = loc.distance(trident.getLocation());
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(@NotNull JavelinThrow.ScoreResult o) {
            return Double.compare(o.distance, this.distance);
        }
    }
}
