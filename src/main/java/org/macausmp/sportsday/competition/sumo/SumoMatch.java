package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.TextUtil;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class SumoMatch {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    protected static final SumoMatchDataType SUMO_MATCH = new SumoMatchDataType();
    private final int number;
    private final UUID[] contestants = new UUID[2];
    private MatchStatus status = MatchStatus.IDLE;
    private UUID winner;
    private UUID loser;

    protected SumoMatch(int number) {
        this.number = number;
    }

    protected void setPlayer(UUID uuid) {
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

    protected void setResult(UUID defeated) {
        if (isEnd())
            return;
        int i = indexOf(defeated);
        if (i == -1)
            return;
        winner = contestants[i ^ 1];
        loser = contestants[i];
        status = MatchStatus.ENDED;
    }

    public OfflinePlayer getFirstPlayer() {
        return Bukkit.getOfflinePlayer(contestants[0]);
    }

    public Component getFirstPlayerName() {
        return Component.text(Objects.requireNonNull(getFirstPlayer().getName()));
    }

    public OfflinePlayer getSecondPlayer() {
        return Bukkit.getOfflinePlayer(contestants[1]);
    }

    public Component getSecondPlayerName() {
        return Component.text(Objects.requireNonNull(getSecondPlayer().getName()));
    }

    public boolean contain(@NotNull UUID uuid) {
        if (!isSet())
            return false;
        return contestants[0].equals(uuid) || contestants[1].equals(uuid);
    }

    @MagicConstant(intValues = {-1, 0, 1})
    private int indexOf(UUID uuid) {
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

    protected void setStatus(MatchStatus status) {
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

    protected static final class SumoMatchDataType implements PersistentDataType<PersistentDataContainer, SumoMatch> {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SumoMatch> getComplexType() {
            return SumoMatch.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SumoMatch complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "number"), INTEGER, complex.number);
            if (complex.contestants[0] != null)
                pdc.set(new NamespacedKey(PLUGIN, "p1"), STRING, complex.contestants[0].toString());
            if (complex.contestants[1] != null)
                pdc.set(new NamespacedKey(PLUGIN, "p2"), STRING, complex.contestants[1].toString());
            boolean end = complex.isEnd();
            pdc.set(new NamespacedKey(PLUGIN, "end"), BOOLEAN, end);
            if (end)
                pdc.set(new NamespacedKey(PLUGIN, "loser"), STRING, complex.loser.toString());
            return pdc;
        }

        @Override
        public @NotNull SumoMatch fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            SumoMatch match = new SumoMatch(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "number"), INTEGER)));
            if (primitive.has(new NamespacedKey(PLUGIN, "p1")))
                match.setPlayer(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p1"), STRING))));
            if (primitive.has(new NamespacedKey(PLUGIN, "p2")))
                match.setPlayer(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p2"), STRING))));
            if (Boolean.TRUE.equals(primitive.get(new NamespacedKey(PLUGIN, "end"), BOOLEAN)))
                match.setResult(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "loser"), STRING))));
            return match;
        }
    }
}
