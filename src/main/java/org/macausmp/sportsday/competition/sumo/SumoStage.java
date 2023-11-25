package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class SumoStage {
    public static final SumoStage FINAL = new SumoStage(Component.translatable("event.sumo.final"), null);
    public static final SumoStage THIRD_PLACE = new SumoStage(Component.translatable("event.sumo.third_place"), FINAL);
    public static final SumoStage SEMI_FINAL = new SumoStage(Component.translatable("event.sumo.semi_final"), THIRD_PLACE);
    public static final SumoStage QUARTER_FINAL = new SumoStage(Component.translatable("event.sumo.quarter_final"), SEMI_FINAL);
    public static final SumoStage ELIMINATE = new SumoStage(Component.translatable("event.sumo.eliminate"), QUARTER_FINAL);

    private final List<SumoMatch> matchList = new ArrayList<>();
    private final Component name;
    private final SumoStage nextStage;
    private SumoMatch currentMatch;
    private int matchIndex = 0;

    public SumoStage(Component name, SumoStage nextStage) {
        this.name = name;
        this.nextStage = nextStage;
    }

    public void nextMatch() {
        currentMatch = matchList.get(matchIndex++);
    }

    public List<SumoMatch> getMatchList() {
        return matchList;
    }

    public boolean hasNextMatch() {
        return matchList.size() - matchIndex > 0;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void resetMatchIndex() {
        matchIndex = 0;
    }

    public Component getName() {
        return name;
    }

    public boolean hasNextStage() {
        return nextStage != null;
    }

    public SumoStage getNextStage() {
        return nextStage;
    }

    public SumoMatch getCurrentMatch() {
        return currentMatch;
    }

    public void resetStage() {
        matchList.clear();
        currentMatch = null;
        matchIndex = 0;
    }
}
