package org.macausmp.sportsday.competition;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ObstacleCourse extends AbstractTrackEvent {
    public ObstacleCourse() {
        super("obstacle_course");
    }

    @Override
    protected void onSetup() {
        getCompetitors().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onEnd(boolean force) {
        Competitions.getOnlineCompetitors().forEach(d -> {
            d.getPlayer().setCollidable(true);
            d.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }

    @Override
    protected void onPractice(@NotNull Player p) {

    }
}
