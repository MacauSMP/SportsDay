package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

/**
 * Represents the competition status.
 */
public enum Status implements ComponentLike {
    UPCOMING("competition.status.upcoming"),
    COMING("competition.status.coming"),
    STARTED("competition.status.started"),
    ENDED("competition.status.ended");

    private final Component name;

    Status(String code) {
        this.name = TextUtil.convert(Component.translatable(code));
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }
}
