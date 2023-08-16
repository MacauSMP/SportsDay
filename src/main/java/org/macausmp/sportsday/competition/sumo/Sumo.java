package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.competition.AbstractCompetition;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IRoundGame;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Sumo extends AbstractCompetition implements IRoundGame {
    private final List<PlayerData> leaderboard = new ArrayList<>();
    private final List<PlayerData> alive = new ArrayList<>();
    private final List<PlayerData> queue = new ArrayList<>();
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
        alive.addAll(getPlayerDataList());
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
        Component c = Component.translatable("competition.sumo.current_stage").args(sumoStage.getName());
        for (int i = 0; i < sumoStage.getRoundList().size();) {
            SumoRound r = sumoStage.getRoundList().get(i++);
            c = c.append(Translation.translatable("competition.sumo.queue").args(Component.text(i), r.getPlayers().get(0).displayName(), r.getPlayers().get(1).displayName()));
            if (i < sumoStage.getRoundList().size() - 1) {
                c = c.appendNewline();
            }
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
            PlayerData data = leaderboard.get(i++);
            c = c.append(Translation.translatable("competition.sumo.rank").args(Component.text(i), Component.text(data.getName())));
            if (i < leaderboard.size()) {
                c = c.appendNewline();
            }
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        SumoRound round = sumoStage.getCurrentRound();
        if (event instanceof PlayerMoveEvent e) {
            Player p = e.getPlayer();
            if (round == null || !round.containPlayer(p)) return;
            if (round.getStatus() == SumoRound.RoundStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() == Material.WATER && round.getStatus() == SumoRound.RoundStatus.STARTED) {
                round.setResult(round.getPlayers().get(0).equals(p) ? round.getPlayers().get(1) : round.getPlayers().get(0), p);
                onRoundEnd();
            }
            return;
        }
        if (event instanceof PlayerQuitEvent e) {
            Player p = e.getPlayer();
            PlayerData d = Competitions.getPlayerData(p.getUniqueId());
            if (!alive.contains(d)) return;
            if (round != null && round.containPlayer(p)) {
                round.setResult(round.getPlayers().get(0).equals(p) ? round.getPlayers().get(1) : round.getPlayers().get(0), p);
                onRoundEnd();
            }
        }
    }

    private @NotNull Player getFromQueue() {
        PlayerData data = queue.get(new Random().nextInt(queue.size()));
        queue.remove(data);
        return data.getPlayer();
    }

    @Override
    public void onRoundStart() {
        SumoRound round = sumoStage.getCurrentRound();
        if (round != null) round.getPlayers().forEach(p -> p.teleport(getLocation()));
        sumoStage.nextRound();
        assert round != null;
        round.setStatus(SumoRound.RoundStatus.COMING);
        List<Player> pl = sumoStage.getCurrentRound().getPlayers();
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
                    if (i != 0) {
                        Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.round_start_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    }
                    if (i-- == 0) {
                        sumoStage.getCurrentRound().setStatus(SumoRound.RoundStatus.STARTED);
                        Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.round_start"));
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
            ItemStack weapon = new ItemStack(Material.BLAZE_ROD);
            weapon.editMeta(meta -> {
                meta.displayName(Translation.translatable("item.sportsday.kb_stick"));
                meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            });
            addRunnable(new BukkitRunnable() {
                int i = 30;
                @Override
                public void run() {
                    if (sumoStage.getCurrentRound() == null || sumoStage.getCurrentRound().getStatus() == SumoRound.RoundStatus.END) {
                        cancel();
                        return;
                    }
                    if (i <= 15 && i % 5 == 0 && i > 0) {
                        Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.knockback_stick_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    }
                    if (i-- == 0) {
                        sumoStage.getCurrentRound().getPlayers().forEach(p -> p.getInventory().setItem(EquipmentSlot.HAND, weapon));
                        Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.knockback_stick_given"));
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    @Override
    public void onRoundEnd() {
        SumoRound round = sumoStage.getCurrentRound();
        if (round.getLoser().isOnline()) {
            getWorld().strikeLightningEffect(round.getLoser().getLocation());
        }
        Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.round_end"));
        Bukkit.broadcast(Translation.translatable("competition.sumo.round_winner").args(round.getWinner().displayName()).color(NamedTextColor.YELLOW));
        round.getPlayers().forEach(p -> p.getInventory().clear());
        // eliminate loser
        if (sumoStage != SumoStage.SEMI_FINAL) {
            for (PlayerData data : alive) {
                if (data.getUUID().equals(round.getLoser().getUniqueId())) {
                    leaderboard.add(0, data);
                    alive.remove(data);
                    break;
                }
            }
        }
        // Pre-assign players to next round
        if (sumoStage == SumoStage.FINAL) {
            leaderboard.add(0, Competitions.getPlayerData(round.getWinner().getUniqueId()));
        } else if (sumoStage == SumoStage.THIRD_PLACE) {
            leaderboard.add(0, Competitions.getPlayerData(round.getWinner().getUniqueId()));
        } else if (sumoStage == SumoStage.SEMI_FINAL) {
            grandFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
            thirdPlace[sumoStage.getRoundIndex() - 1] = round.getLoser();
        } else if (sumoStage == SumoStage.QUARTER_FINAL) {
            semiFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
        }
        // if there are still rounds left in this stage
        if (sumoStage.getRoundRemaining() != 0) {
            SumoRound r = sumoStage.getRoundList().get(sumoStage.getRoundIndex());
            Bukkit.broadcast(Translation.translatable("competition.sumo.next_queue").args(r.getPlayers().get(0).displayName(), r.getPlayers().get(1).displayName()));
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
                Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.next_round_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    onRoundStart();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private void nextSumoStage() {
        if (sumoStage.getCurrentRound() != null) sumoStage.getCurrentRound().getPlayers().forEach(p -> p.teleport(getLocation()));
        // if the number of players is less than 8 go to the next stage
        if (alive.size() <= 8 && sumoStage.hasNextStage()) {
            sumoStage = sumoStage.getNextStage();
        }
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
        } else {
            for (int i = 0; i < alive.size() - 8; i++) {
                sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 7;
            @Override
            public void run() {
                if (i <= 5 && i > 0) {
                    Bukkit.getServer().sendActionBar(Translation.translatable("competition.sumo.next_stage_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                }
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
    public List<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
