package org.macausmp.sportsday.event;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Track;

import java.util.Objects;

public class ObstacleCourseEvent extends TrackEvent {
    public ObstacleCourseEvent(@NotNull Sport sport, @NotNull Track track, @Nullable PersistentDataContainer save) {
        super(sport, track, save);
    }

    @Override
    protected void onEventStart() {
        SportsDay.CONTESTANTS.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
    }

    @Override
    protected void onClose() {
        SportsDay.CONTESTANTS.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    @Override
    protected void onCompletedLap(@NotNull Player player) {
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
    }
}
