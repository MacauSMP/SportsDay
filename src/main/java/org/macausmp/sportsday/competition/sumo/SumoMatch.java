package org.macausmp.sportsday.competition.sumo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class SumoMatch {
    private final UUID[] contestants = new UUID[2];
    private MatchStatus status = MatchStatus.IDLE;
    private UUID winner;
    private UUID loser;

    public void setPlayer(UUID uuid) {
        if (isSet())
            return;
        contestants[contestants[0] == null ? 0 : 1] = uuid;
    }

    public boolean isSet() {
        return contestants[0] != null && contestants[1] != null;
    }

    public void setResult(UUID defeated) {
        if (status == MatchStatus.END)
            return;
        int i = indexOf(defeated);
        if (i == -1)
            return;
        winner = contestants[i ^ 1];
        loser = contestants[i];
        status = MatchStatus.END;
    }

    public Player[] getPlayers() {
        return new Player[]{Bukkit.getPlayer(contestants[0]), Bukkit.getPlayer(contestants[1])};
    }

    public boolean contain(@NotNull UUID uuid) {
        if (!isSet())
            return false;
        return contestants[0].equals(uuid) || contestants[1].equals(uuid);
    }

    @MagicConstant(intValues = {-1, 0, 1})
    public int indexOf(UUID uuid) {
        if (!isSet())
            return -1;
        if (contestants[0].equals(uuid))
            return 0;
        if (contestants[1].equals(uuid))
            return 1;
        return -1;
    }

    public void forEachPlayer(@NotNull Consumer<Player> consumer) {
        if (!isSet())
            return;
        consumer.accept(Bukkit.getPlayer(contestants[0]));
        consumer.accept(Bukkit.getPlayer(contestants[1]));
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public UUID getWinner() {
        return winner;
    }

    public UUID getLoser() {
        return loser;
    }

    public enum MatchStatus {
        IDLE,
        COMING,
        STARTED,
        END
    }
}
