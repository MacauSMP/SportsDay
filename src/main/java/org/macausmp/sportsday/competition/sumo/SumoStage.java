package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SumoStage {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    protected static final SumoStageDataType SUMO_STAGE = new SumoStageDataType();
    private final int number;
    private final Stage stage;
    private final List<SumoMatch> matchList = new ArrayList<>();
    private int matchIndex = -1;
    private SumoMatch currentMatch;

    protected SumoStage(int number, Stage stage) {
        this.number = number;
        this.stage = stage;
    }

    public int getNumber() {
        return number;
    }

    public Stage getStage() {
        return stage;
    }

    public List<SumoMatch> getMatchList() {
        return matchList;
    }

    public boolean hasNextStage() {
        return stage != Stage.FINAL;
    }

    public SumoMatch getCurrentMatch() {
        return currentMatch;
    }

    public int getCurrentMatchIndex() {
        return matchIndex;
    }

    public void nextMatch() {
        currentMatch = matchList.get(++matchIndex);
    }

    protected SumoMatch newMatch() {
        SumoMatch match = new SumoMatch(matchList.size() + 1);
        matchList.add(match);
        return match;
    }

    public boolean hasNextMatch() {
        return matchIndex + 1 < matchList.size();
    }

    public SumoMatch getNextMatch() {
        return hasNextMatch() ? matchList.get(matchIndex + 1) : null;
    }

    public Component getName() {
        return stage.name;
    }

    public Material getIcon() {
        return stage.icon;
    }

    public enum Stage {
        ELIMINATE(Component.translatable("event.sumo.eliminate"), Material.IRON_BLOCK),
        QUARTER_FINAL(Component.translatable("event.sumo.quarter_final"), Material.LAPIS_BLOCK),
        SEMI_FINAL(Component.translatable("event.sumo.semi_final"), Material.REDSTONE_BLOCK),
        THIRD_PLACE(Component.translatable("event.sumo.third_place"), Material.COPPER_BLOCK),
        FINAL(Component.translatable("event.sumo.final"), Material.GOLD_BLOCK);

        final Component name;
        final Material icon;

        Stage(Component name, Material icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    protected static final class SumoStageDataType implements PersistentDataType<PersistentDataContainer, SumoStage> {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SumoStage> getComplexType() {
            return SumoStage.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SumoStage complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "number"), INTEGER, complex.number);
            pdc.set(new NamespacedKey(PLUGIN, "stage"), STRING, complex.stage.name());
            pdc.set(new NamespacedKey(PLUGIN, "index"), INTEGER,
                    complex.matchIndex - (complex.currentMatch == null || complex.currentMatch.isEnd() ? 0 : 1));
            pdc.set(new NamespacedKey(PLUGIN, "match_list"), LIST.listTypeFrom(SumoMatch.SUMO_MATCH), complex.matchList);
            return pdc;
        }

        @Override
        public @NotNull SumoStage fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            int number = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "number"), INTEGER));
            Stage stage = Stage.valueOf(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "stage"), STRING)));
            SumoStage sumoStage = new SumoStage(number, stage);
            sumoStage.matchList.addAll(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "match_list"),
                    LIST.listTypeFrom(SumoMatch.SUMO_MATCH))));
            sumoStage.matchIndex = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "index"), INTEGER));
            if (sumoStage.matchIndex != -1)
                sumoStage.currentMatch = sumoStage.matchList.get(sumoStage.matchIndex);
            return sumoStage;
        }
    }
}
