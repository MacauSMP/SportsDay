package org.macausmp.sportsday.competition;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

public class ObstacleCourse extends AbstractTrackEvent {
    public ObstacleCourse() {
        super("obstacle_course");
    }

    @Override
    protected void onSetup() {
        SportsDay.COMPETITOR.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onEnd(boolean force) {
        SportsDay.COMPETITOR.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    @Override
    protected void onPractice(@NotNull Player p) {

    }
}
