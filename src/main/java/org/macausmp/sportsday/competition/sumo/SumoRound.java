package org.macausmp.sportsday.competition.sumo;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SumoRound {
    private final List<Player> competitors = new ArrayList<>();
    private RoundStatus status = RoundStatus.IDLE;
    private Player winner;
    private Player loser;

    public SumoRound(Player p1, Player p2) {
        this.competitors.addAll(List.of(p1, p2));
    }

    public void setResult(Player winner, Player loser) {
        this.winner = winner;
        this.loser = loser;
        setStatus(RoundStatus.END);
    }

    public List<Player> getCompetitors() {
        return competitors;
    }

    public boolean contain(@NotNull Player player) {
        return competitors.contains(player);
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getLoser() {
        return loser;
    }

    public enum RoundStatus {
        IDLE,
        COMING,
        STARTED,
        END
    }
}
