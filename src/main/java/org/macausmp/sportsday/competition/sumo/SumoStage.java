package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SumoStage {
    private final int number;
    private final Stage stage;
    private final List<SumoMatch> matchList = new ArrayList<>();
    private SumoMatch currentMatch;
    private int matchIndex = 0;

    public SumoStage(int number, Stage stage) {
        this.number = number;
        this.stage = stage;
    }

    public void nextMatch() {
        currentMatch = matchList.get(matchIndex++);
    }

    public SumoMatch newMatch() {
        SumoMatch match = new SumoMatch(matchList.size() + 1);
        matchList.add(match);
        return match;
    }

    public List<SumoMatch> getMatchList() {
        return matchList;
    }

    public boolean hasNextMatch() {
        return matchIndex < matchList.size();
    }

    public int getNumber() {
        return number;
    }

    public Stage getStage() {
        return stage;
    }

    public Component getName() {
        return stage.name;
    }

    public Material getIcon() {
        return stage.icon;
    }

    public boolean hasNextStage() {
        return stage != Stage.FINAL;
    }

    public SumoMatch getCurrentMatch() {
        return currentMatch;
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
}
