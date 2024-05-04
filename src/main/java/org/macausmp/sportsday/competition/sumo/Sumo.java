package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.AbstractEvent;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IFieldEvent;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.*;

public class Sumo extends AbstractEvent implements IFieldEvent {
    private final List<CompetitorData> leaderboard = new ArrayList<>();
    private final Set<CompetitorData> alive = new HashSet<>();
    private final List<CompetitorData> queue = new ArrayList<>();
    private SumoStage[] stages;
    private int stageIndex = 0;
    private final boolean weapon = PLUGIN.getConfig().getBoolean(getID() + ".enable_weapon");
    private final int time = PLUGIN.getConfig().getInt(getID() + ".weapon_time");

    public Sumo() {
        super("sumo");
    }

    @Override
    public void onSetup() {
        alive.clear();
        alive.addAll(getCompetitors());
        queue.clear();
        queue.addAll(alive);
        int stage = Math.max(33 - Integer.numberOfLeadingZeros(getCompetitors().size() - 1), 3);
        stages = new SumoStage[stage];
        stageIndex = 0;
        for (int i = 1; i <= stage; i++) {
            int j = SumoStage.Stage.values().length;
            stages[stage - i] = new SumoStage(SumoStage.Stage.values()[i < j ? j - i : 0]);
        }
        int size = switch (stages[1].getStage()) {
            case SEMI_FINAL -> queue.size() - 4;
            case QUARTER_FINAL -> queue.size() - 8;
            default -> queue.size() / 2;
        };
        for (int i = 0; i < size; i++) {
            SumoMatch match = new SumoMatch();
            match.setPlayer(getFromQueue());
            match.setPlayer(getFromQueue());
            stages[0].getMatchList().add(match);
        }
        stages[stages.length - 2].getMatchList().add(new SumoMatch()); // Third place
        stages[stages.length - 1].getMatchList().add(new SumoMatch()); // Final
        stageSetup();
    }

    private void stageSetup() {
        Component c = Component.translatable("event.sumo.current_stage").args(getSumoStage().getName());
        for (int i = 0; i < getSumoStage().getMatchList().size();) {
            SumoMatch m = getSumoStage().getMatchList().get(i++);
            c = c.appendNewline().append(Component.translatable("event.sumo.queue").args(Component.text(i), m.getPlayers()[0].displayName(), m.getPlayers()[1].displayName()));
        }
        Bukkit.broadcast(c);
    }

    @Override
    public void onStart() {
        onMatchStart();
    }

    @Override
    public void onEnd(boolean force) {
        if (force) return;
        Component c = Component.text().build();
        for (int i = 0; i < leaderboard.size();) {
            CompetitorData data = leaderboard.get(i++);
            c = c.append(Component.translatable("event.sumo.rank").args(Component.text(i), Component.text(data.getName())));
            if (i < leaderboard.size()) c = c.appendNewline();
            if (i <= 3) data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (Competitions.getCurrentEvent() == this && getStatus() == Status.STARTED && Competitions.isCompetitor(p)) {
            SumoMatch match = getSumoStage().getCurrentMatch();
            if (match == null || !match.contain(p.getUniqueId())) return;
            if (match.getStatus() == SumoMatch.MatchStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() == Material.WATER && match.getStatus() == SumoMatch.MatchStatus.STARTED) {
                match.setResult(p.getUniqueId());
                onMatchEnd();
            }
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Competitions.getCurrentEvent() == this && getStatus() == Status.STARTED && Competitions.isCompetitor(p)) {
            SumoMatch match = getSumoStage().getCurrentMatch();
            UUID uuid = p.getUniqueId();
            CompetitorData d = Competitions.getCompetitor(uuid);
            if (!alive.contains(d)) return;
            if (match != null && match.contain(uuid)) {
                match.setResult(uuid);
                onMatchEnd();
            }
        }
    }

    public void onDisqualification(@NotNull CompetitorData competitor) {
        super.onDisqualification(competitor);
        alive.remove(competitor);
        queue.remove(competitor);
        SumoMatch match = getSumoStage().getCurrentMatch();
        UUID uuid = competitor.getUUID();
        if (match != null && match.contain(uuid)) {
            match.setResult(uuid);
            onMatchEnd();
        }
    }

    private @NotNull UUID getFromQueue() {
        return queue.remove(new Random().nextInt(queue.size())).getUUID();
    }

    @Override
    public void onMatchStart() {
        SumoMatch prev = getSumoStage().getCurrentMatch();
        if (prev != null) prev.forEachPlayer(p -> p.teleport(getLocation()));
        getSumoStage().nextMatch();
        SumoMatch match = getSumoStage().getCurrentMatch();
        match.setStatus(SumoMatch.MatchStatus.COMING);
        Player[] pa = match.getPlayers();
        Player p1 = pa[0];
        Player p2 = pa[1];
        if (Competitions.isCompetitor(p1) && Competitions.isCompetitor(p2)) {
            if (p1.isOnline() && p2.isOnline()) {
                p1.teleport(Objects.requireNonNull(PLUGIN.getConfig().getLocation(getID() + ".p1-location")));
                p2.teleport(Objects.requireNonNull(PLUGIN.getConfig().getLocation(getID() + ".p2-location")));
                match.forEachPlayer(p -> p.getInventory().clear());
                addRunnable(new BukkitRunnable() {
                    int i = 5;
                    @Override
                    public void run() {
                        if (i != 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_start_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                        if (i-- == 0) {
                            match.setStatus(SumoMatch.MatchStatus.STARTED);
                            Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_start"));
                            giveWeapon();
                            cancel();
                        }
                    }
                }.runTaskTimer(PLUGIN, 0L, 20L));
                return;
            }
            match.setResult((p1.isOnline() ? p2 : p1).getUniqueId());
            onMatchEnd();
            return;
        }
        match.setResult((Competitions.isCompetitor(p1) ? p2 : p1).getUniqueId());
        onMatchEnd();
    }

    private void giveWeapon() {
        if (weapon) {
            addRunnable(new BukkitRunnable() {
                int i = time;
                @Override
                public void run() {
                    SumoMatch match = getSumoStage().getCurrentMatch();
                    if (match == null || match.getStatus() == SumoMatch.MatchStatus.END) {
                        cancel();
                        return;
                    }
                    if (i <= 15 && i % 5 == 0 && i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.knockback_stick.countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        match.forEachPlayer(p -> p.getInventory().setItem(EquipmentSlot.HAND, weapon(p)));
                        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.knockback_stick.given"));
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    private @NotNull ItemStack weapon(@NotNull Player p) {
        ItemStack weapon = ItemUtil.setBind(ItemUtil.item(Material.BLAZE_ROD, null, "item.sportsday.kb_stick"));
        Material material = PlayerCustomize.getWeaponSkin(p);
        if (material != null) weapon.setType(material);
        weapon.editMeta(meta -> {
            meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
        });
        return weapon;
    }

    @Override
    public void onMatchEnd() {
        SumoMatch match = getSumoStage().getCurrentMatch();
        if (match.getLoser() != null) getWorld().strikeLightningEffect(Objects.requireNonNull(Bukkit.getPlayer(match.getLoser())).getLocation());
        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_end"));
        Bukkit.broadcast(Component.translatable("event.sumo.match_winner").args(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName()).color(NamedTextColor.YELLOW));
        match.forEachPlayer(p -> p.getInventory().clear());
        // eliminate loser
        if (getSumoStage().getStage() != SumoStage.Stage.SEMI_FINAL) {
            for (CompetitorData data : alive) {
                if (data.getUUID().equals(match.getLoser())) {
                    leaderboard.addFirst(data);
                    alive.remove(data);
                    break;
                }
            }
        } else {
            stages[stages.length - 2].getMatchList().getFirst().setPlayer(match.getLoser());
            stages[stages.length - 1].getMatchList().getFirst().setPlayer(match.getWinner());
        }
        // if stage == THIRD_PLACE or FINAL
        if (stageIndex >= stages.length - 2) leaderboard.addFirst(Competitions.getCompetitor(match.getWinner()));
        // If this stage is not over
        if (getSumoStage().hasNextMatch()) {
            SumoMatch m = getSumoStage().getCurrentMatch();
            Bukkit.broadcast(Component.translatable("event.sumo.next_queue").args(m.getPlayers()[0].displayName(), m.getPlayers()[1].displayName()));
            nextMatch();
        } else {
            if (getSumoStage().hasNextStage()) {
                nextSumoStage();
            } else {
                end(false);
            }
        }
    }

    @Override
    public void nextMatch() {
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_match_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    onMatchStart();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private void nextSumoStage() {
        // If this is not the first stage of the event, it means there are some players in the arena
        SumoMatch prev = getSumoStage().getCurrentMatch();
        if (prev != null) prev.forEachPlayer(p -> p.teleport(getLocation()));
        if (++stageIndex < stages.length - 2) {
            queue.clear();
            queue.addAll(alive);
            int size = switch (stages[stageIndex + 1].getStage()) {
                case THIRD_PLACE -> 2;
                case SEMI_FINAL -> 4;
                case QUARTER_FINAL -> queue.size() - 8;
                case ELIMINATE -> queue.size() / 2;
                default -> 0; // Actually unreachable
            };
            for (int i = 0; i < size; i++) {
                SumoMatch match = new SumoMatch();
                match.setPlayer(queue.removeFirst().getUUID());
                match.setPlayer(queue.removeFirst().getUUID());
                getSumoStage().getMatchList().add(match);
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 7;
            @Override
            public void run() {
                if (i <= 5 && i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_stage_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    nextMatch();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    public SumoStage getSumoStage() {
        return stages[stageIndex];
    }

    @Override
    protected void onPractice(@NotNull Player p) {
    }

    @Override
    public List<CompetitorData> getLeaderboard() {
        return leaderboard;
    }
}
