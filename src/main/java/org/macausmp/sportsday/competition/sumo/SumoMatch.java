package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

import java.util.UUID;
import java.util.function.Consumer;

public class SumoMatch {
    private final int number;
    private final UUID[] contestants = new UUID[2];
    private MatchStatus status = MatchStatus.IDLE;
    private UUID winner;
    private UUID loser;

    SumoMatch(int number) {
        this.number = number;
    }

    public void setPlayer(UUID uuid) {
        if (isSet())
            return;
        contestants[contestants[0] == null ? 0 : 1] = uuid;
    }

    public boolean isSet() {
        return contestants[0] != null && contestants[1] != null;
    }

    public boolean isEnd() {
        return status == MatchStatus.ENDED;
    }

    public void setResult(UUID defeated) {
        if (isEnd())
            return;
        int i = indexOf(defeated);
        if (i == -1)
            return;
        winner = contestants[i ^ 1];
        loser = contestants[i];
        status = MatchStatus.ENDED;
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

    public int getNumber() {
        return number;
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
        IDLE("competition.status.idle"),
        COMING("competition.status.coming"),
        STARTED("competition.status.started"),
        ENDED("competition.status.ended");

        private final Component name;

        MatchStatus(String code) {
            this.name = TextUtil.convert(Component.translatable(code));
        }

        public Component getName() {
            return name;
        }
    }
}
