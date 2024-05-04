package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Deprecated
public class ScoreboardHandler {
    private static final SportsDay PLUGIN = SportsDay.getInstance();

    @Deprecated
    public void setScoreboard(Player p) {
        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                Scoreboard scoreboard = PLUGIN.getServer().getScoreboardManager().getNewScoreboard();
                Objective o = scoreboard.registerNewObjective("sportsday", Criteria.DUMMY, Component.translatable("scoreboard.title").color(NamedTextColor.GOLD));
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
                Entry event = new Entry(o, "competition", "scoreboard.competition");
                Entry status = new Entry(o, "status", "scoreboard.status");
                Entry count = new Entry(o, "competitor_count", "scoreboard.competitor_count").setScore(8);
                Entry number = new Entry(o, "number", "scoreboard.number");
                Entry score = new Entry(o, "score", "scoreboard.score");
                newline(o).setScore(5);
                Entry time = new Entry(o, "time", "scoreboard.time").setScore(4);
                Entry ping = new Entry(o, "ping", "scoreboard.ping").setScore(3);
                newline(o).setScore(2);
                o.getScore(Objects.requireNonNull(PLUGIN.getConfig().getString("server_ip"))).setScore(1);
                new BukkitRunnable() {
                    final Score line = newline(o);
                    @Override
                    public void run() {
                        if (Competitions.getCurrentEvent() != null) {
                            event.suffix(Competitions.getCurrentEvent().getName()).setScore(11);
                            status.suffix(Competitions.getCurrentEvent().getStatus().getName()).setScore(10);
                            line.setScore(9);
                        } else {
                            event.resetScore();
                            status.resetScore();
                            line.resetScore();
                        }
                        count.suffix(Component.translatable("%s/%s").args(Component.text(Competitions.getOnlineCompetitors().size()), Component.text(PLUGIN.getServer().getOnlinePlayers().size())));
                        if (Competitions.isCompetitor(p)) {
                            number.suffix(Component.text(Competitions.getCompetitor(p.getUniqueId()).getNumber())).setScore(7);
                            score.suffix(Component.text(Competitions.getCompetitor(p.getUniqueId()).getScore())).setScore(6);
                        } else {
                            number.resetScore();
                            score.resetScore();
                        }
                        time.suffix(Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                        ping.suffix(Component.text(p.getPing()));
                    }
                }.runTaskTimer(PLUGIN, 0L, 20L);
                p.setScoreboard(scoreboard);
            }
        }.runTaskLater(PLUGIN, d);
    }

    private @NotNull Score newline(@NotNull Objective o) {
        StringBuilder b = new StringBuilder(" ");
        while (o.getScore(b.toString()).isScoreSet()) {
            b.append(" ");
        }
        return o.getScore(b.toString());
    }

    private final static class Entry {
        private final Objective objective;
        private final Team team;
        private final String entry;

        public Entry(@NotNull Objective objective, String entry, String code) {
            this.objective = objective;
            this.team = Objects.requireNonNull(objective.getScoreboard()).registerNewTeam(entry);
            this.entry = LegacyComponentSerializer.legacySection().serialize(Component.translatable(code));
            this.team.addEntry(this.entry);
        }

        public Entry suffix(Component suffix) {
            team.suffix(suffix);
            return this;
        }

        public Entry setScore(int score) {
            objective.getScore(entry).setScore(score);
            return this;
        }

        public void resetScore() {
            objective.getScore(entry).resetScore();
        }
    }
}
