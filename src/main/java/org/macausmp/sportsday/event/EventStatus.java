package org.macausmp.sportsday.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

/**
 * Represents the event status.
 */
public enum EventStatus implements ComponentLike {
    UPCOMING("event.status.upcoming"),
    PROCESSING("event.status.processing"),
    PAUSED("event.status.paused"),
    ENDED("event.status.ended"),
    CLOSED("event.status.closed");

    private final Component name;

    EventStatus(String code) {
        this.name = TextUtil.convert(Component.translatable(code));
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }
}
