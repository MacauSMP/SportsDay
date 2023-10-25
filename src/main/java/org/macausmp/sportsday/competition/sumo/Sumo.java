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
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Sumo extends AbstractEvent implements IFieldEvent {
    private final List<CompetitorData> leaderboard = new ArrayList<>();
    private final List<CompetitorData> alive = new ArrayList<>();
    private final List<CompetitorData> queue = new ArrayList<>();
    private SumoStage sumoStage = SumoStage.ELIMINATE;
    private final Player[] grandFinal = new Player[2];
    private final Player[] thirdPlace = new Player[2];
    private final Player[] semiFinal = new Player[4];
    private final boolean weapon = PLUGIN.getConfig().getBoolean(getID() + ".enable_weapon");

    public Sumo() {
        super("sumo");
    }

    @Override
    public void onSetup() {
        alive.clear();
        alive.addAll(getCompetitors());
        queue.clear();
        queue.addAll(alive);
        SumoStage.FINAL.resetStage();
        SumoStage.THIRD_PLACE.resetStage();
        SumoStage.SEMI_FINAL.resetStage();
        SumoStage.QUARTER_FINAL.resetStage();
        SumoStage.ELIMINATE.resetStage();
        int stageRound;
        if (queue.size() <= 4) {
            sumoStage = SumoStage.SEMI_FINAL;
            stageRound = 2;
        } else if (queue.size() <= 8) {
            sumoStage = SumoStage.QUARTER_FINAL;
            stageRound = queue.size() - 4;
        } else {
            sumoStage = SumoStage.ELIMINATE;
            stageRound = queue.size() - 8;
        }
        for (int i = 0; i < stageRound; i++) {
            sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
        }
        stageSetup();
    }

    private void stageSetup() {
        sumoStage.resetRoundIndex();
        Component c = Component.translatable("event.sumo.current_stage").args(sumoStage.getName());
        for (int i = 0; i < sumoStage.getRoundList().size();) {
            SumoRound r = sumoStage.getRoundList().get(i++);
            c = c.append(Component.translatable("event.sumo.queue").args(Component.text(i), r.getCompetitors().get(0).displayName(), r.getCompetitors().get(1).displayName()));
            if (i < sumoStage.getRoundList().size() - 1) c = c.appendNewline();
        }
        Bukkit.broadcast(c);
    }

    @Override
    public void onStart() {
        onRoundStart();
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

    @Override
    protected void onPractice(@NotNull Player p) {
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
            SumoRound round = sumoStage.getCurrentRound();
            if (round == null || !round.contain(p)) return;
            if (round.getStatus() == SumoRound.RoundStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() == Material.WATER && round.getStatus() == SumoRound.RoundStatus.STARTED) {
                round.setResult(round.getCompetitors().get(0).equals(p) ? round.getCompetitors().get(1) : round.getCompetitors().get(0), p);
                onRoundEnd();
            }
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if (event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) {
            SumoRound round = sumoStage.getCurrentRound();
            CompetitorData d = Competitions.getCompetitor(p.getUniqueId());
            if (!alive.contains(d)) return;
            if (round != null && round.contain(p)) {
                round.setResult(round.getCompetitors().get(0).equals(p) ? round.getCompetitors().get(1) : round.getCompetitors().get(0), p);
                onRoundEnd();
            }
        }
    }

    private @NotNull Player getFromQueue() {
        CompetitorData data = queue.get(new Random().nextInt(queue.size()));
        queue.remove(data);
        return data.getPlayer();
    }

    @Override
    public void onRoundStart() {
        SumoRound round = sumoStage.getCurrentRound();
        if (round != null) round.getCompetitors().forEach(p -> p.teleport(getLocation()));
        sumoStage.nextRound();
        assert round != null;
        round.setStatus(SumoRound.RoundStatus.COMING);
        List<Player> pl = sumoStage.getCurrentRound().getCompetitors();
        Player p1 = pl.get(0);
        Player p2 = pl.get(1);
        if (p1.isOnline() && p2.isOnline()) {
            p1.teleport(Objects.requireNonNull(PLUGIN.getConfig().getLocation(getID() + ".p1-location")));
            p2.teleport(Objects.requireNonNull(PLUGIN.getConfig().getLocation(getID() + ".p2-location")));
            pl.forEach(p -> p.getInventory().clear());
            addRunnable(new BukkitRunnable() {
                int i = 5;
                @Override
                public void run() {
                    if (i != 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.round_start_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        sumoStage.getCurrentRound().setStatus(SumoRound.RoundStatus.STARTED);
                        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.round_start"));
                        giveWeapon();
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
            return;
        }
        round.setResult(p1.isOnline() ? p1 : p2, p1.isOnline() ? p2 : p1);
        onRoundEnd();
    }

    private void giveWeapon() {
        if (weapon) {
            addRunnable(new BukkitRunnable() {
                int i = 30;
                @Override
                public void run() {
                    if (sumoStage.getCurrentRound() == null || sumoStage.getCurrentRound().getStatus() == SumoRound.RoundStatus.END) {
                        cancel();
                        return;
                    }
                    if (i <= 15 && i % 5 == 0 && i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.knockback_stick.countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        sumoStage.getCurrentRound().getCompetitors().forEach(p -> p.getInventory().setItem(EquipmentSlot.HAND, weapon(p)));
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
    public void onRoundEnd() {
        SumoRound round = sumoStage.getCurrentRound();
        if (round.getLoser().isOnline()) getWorld().strikeLightningEffect(round.getLoser().getLocation());
        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.round_end"));
        Bukkit.broadcast(Component.translatable("event.sumo.round_winner").args(round.getWinner().displayName()).color(NamedTextColor.YELLOW));
        round.getCompetitors().forEach(p -> p.getInventory().clear());
        // eliminate loser
        if (sumoStage != SumoStage.SEMI_FINAL) {
            for (CompetitorData data : alive) {
                if (data.getUUID().equals(round.getLoser().getUniqueId())) {
                    leaderboard.add(0, data);
                    alive.remove(data);
                    break;
                }
            }
        }
        // Pre-assign players to next round
        if (sumoStage == SumoStage.FINAL) {
            leaderboard.add(0, Competitions.getCompetitor(round.getWinner().getUniqueId()));
        } else if (sumoStage == SumoStage.THIRD_PLACE) {
            leaderboard.add(0, Competitions.getCompetitor(round.getWinner().getUniqueId()));
        } else if (sumoStage == SumoStage.SEMI_FINAL) {
            grandFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
            thirdPlace[sumoStage.getRoundIndex() - 1] = round.getLoser();
        } else if (sumoStage == SumoStage.QUARTER_FINAL) {
            semiFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
        }
        // If this stage is not over
        if (sumoStage.getRoundRemaining() != 0) {
            SumoRound r = sumoStage.getRoundList().get(sumoStage.getRoundIndex());
            Bukkit.broadcast(Component.translatable("event.sumo.next_queue").args(r.getCompetitors().get(0).displayName(), r.getCompetitors().get(1).displayName()));
            nextRound();
        } else {
            if (sumoStage != SumoStage.FINAL) {
                nextSumoStage();
            } else {
                end(false);
            }
        }
    }

    @Override
    public void nextRound() {
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_round_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    onRoundStart();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private void nextSumoStage() {
        // If this is not the first stage of the event, it means there are some players in the arena
        if (sumoStage.getCurrentRound() != null) sumoStage.getCurrentRound().getCompetitors().forEach(p -> p.teleport(getLocation()));
        // If the number of players is less than 8 go to the next stage
        if (alive.size() <= 8 && sumoStage.hasNextStage()) sumoStage = sumoStage.getNextStage();
        // Assign players to their round
        if (sumoStage == SumoStage.FINAL) {
            sumoStage.getRoundList().add(new SumoRound(grandFinal[0], grandFinal[1]));
        } else if (sumoStage == SumoStage.THIRD_PLACE) {
            sumoStage.getRoundList().add(new SumoRound(thirdPlace[0], thirdPlace[1]));
        } else if (sumoStage == SumoStage.SEMI_FINAL) {
            for (int i = 0; i < alive.size() / 2; i++) {
                Player p1 = semiFinal[i] != null ? semiFinal[i] : getFromQueue();
                Player p2 = semiFinal[i + 1] != null ? semiFinal[i + 1] : getFromQueue();
                sumoStage.getRoundList().add(new SumoRound(p1, p2));
            }
        } else if (sumoStage == SumoStage.QUARTER_FINAL) {
            for (int i = 0; i < alive.size() / 2; i++) {
                sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        } else { // Eliminate stage, number of rounds = n - 8, 8 is the total number of players in the quarter-final
            for (int i = 0; i < alive.size() - 8; i++) {
                sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 7;
            @Override
            public void run() {
                if (i <= 5 && i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_stage_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    nextRound();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    public SumoStage getSumoStage() {
        return sumoStage;
    }

    @Override
    public List<CompetitorData> getLeaderboard() {
        return leaderboard;
    }
}
