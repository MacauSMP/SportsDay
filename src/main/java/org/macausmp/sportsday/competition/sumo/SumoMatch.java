package org.macausmp.sportsday.competition.sumo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class SumoMatch {
    private final UUID[] competitors = new UUID[2];
    private MatchStatus status = MatchStatus.IDLE;
    private UUID winner;
    private UUID loser;

    public void setPlayer(Player player) {
        if (isSet()) return;
        competitors[competitors[0] == null ? 0 : 1] = player.getUniqueId();
    }

    public boolean isSet() {
        return competitors[0] != null && competitors[1] != null;
    }

    public void setResult(Player loser) {
        if (this.status == MatchStatus.END) return;
        int i = indexOf(loser.getUniqueId());
        if (i == -1) return;
        this.winner = competitors[i ^ 1];
        this.loser = competitors[i];
        this.status = MatchStatus.END;
    }

    public Player[] getCompetitors() {
        return new Player[]{Bukkit.getPlayer(competitors[0]), Bukkit.getPlayer(competitors[1])};
    }

    public boolean contain(@NotNull Player player) {
        if (!isSet()) return false;
        return competitors[0].equals(player.getUniqueId()) || competitors[1].equals(player.getUniqueId());
    }

    public int indexOf(UUID uuid) {
        if (!isSet()) return -1;
        if (competitors[0].equals(uuid)) return 0;
        if (competitors[1].equals(uuid)) return 1;
        return -1;
    }

    public void forEachPlayer(@NotNull Consumer<Player> consumer) {
        if (!isSet()) return;
        consumer.accept(Bukkit.getPlayer(competitors[0]));
        consumer.accept(Bukkit.getPlayer(competitors[1]));
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Player getWinner() {
        return winner != null ? Bukkit.getPlayer(winner) : null;
    }

    public Player getLoser() {
        return loser != null ? Bukkit.getPlayer(loser) : null;
    }

    public enum MatchStatus {
        IDLE,
        COMING,
        STARTED,
        END
    }
}
