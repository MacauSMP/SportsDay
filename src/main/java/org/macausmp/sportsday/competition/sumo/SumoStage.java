package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class SumoStage {
    private final Stage stage;
    private final List<SumoMatch> matchList = new ArrayList<>();
    private SumoMatch currentMatch;
    private int matchIndex = 0;

    public SumoStage(Stage stage) {
        this.stage = stage;
    }

    public void nextMatch() {
        currentMatch = matchList.get(matchIndex++);
    }

    public List<SumoMatch> getMatchList() {
        return matchList;
    }

    public boolean hasNextMatch() {
        return matchIndex < matchList.size();
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public Stage getStage() {
        return stage;
    }

    public Component getName() {
        return stage.name;
    }

    public boolean hasNextStage() {
        return stage != Stage.FINAL;
    }

    public SumoMatch getCurrentMatch() {
        return currentMatch;
    }

    public enum Stage {
        ELIMINATE(Component.translatable("event.sumo.eliminate")),
        QUARTER_FINAL(Component.translatable("event.sumo.quarter_final")),
        SEMI_FINAL(Component.translatable("event.sumo.semi_final")),
        THIRD_PLACE(Component.translatable("event.sumo.third_place")),
        FINAL(Component.translatable("event.sumo.final"));

        final Component name;

        Stage(Component name) {
            this.name = name;
        }
    }
}
