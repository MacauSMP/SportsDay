package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.util.TextUtil;

/**
 * Represents the competition status.
 */
public enum Status {
    IDLE("competition.status.idle"),
    COMING("competition.status.coming"),
    STARTED("competition.status.started"),
    ENDED("competition.status.ended");

    private final Component name;

    Status(String code) {
        this.name = TextUtil.convert(Component.translatable(code));
    }

    public Component getName() {
        return name;
    }
}
