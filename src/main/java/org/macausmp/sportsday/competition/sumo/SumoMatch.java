package org.macausmp.sportsday.competition.sumo;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SumoMatch {
    private final List<Player> competitors = new ArrayList<>();
    private MatchStatus status = MatchStatus.IDLE;
    private Player winner;
    private Player loser;

    public SumoMatch(Player p1, Player p2) {
        this.competitors.addAll(List.of(p1, p2));
    }

    public void setResult(Player winner, Player loser) {
        this.winner = winner;
        this.loser = loser;
        setStatus(MatchStatus.END);
    }

    public List<Player> getCompetitors() {
        return competitors;
    }

    public boolean contain(@NotNull Player player) {
        return competitors.contains(player);
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getLoser() {
        return loser;
    }

    public enum MatchStatus {
        IDLE,
        COMING,
        STARTED,
        END
    }
}
