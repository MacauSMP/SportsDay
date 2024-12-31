package org.macausmp.sportsday.customize;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

public enum GraffitiSpray implements ComponentLike {
    GGWP("customize.graffiti_spray.ggwp"),
    DISMAL("customize.graffiti_spray.d"),
    CRAZY("customize.graffiti_spray.c"),
    BADASS("customize.graffiti_spray.b"),
    APOCALYPTIC("customize.graffiti_spray.a"),
    SAVAGE("customize.graffiti_spray.s"),
    SICK_SKILLS("customize.graffiti_spray.ss"),
    SMOKIN_SEXY_STYLE("customize.graffiti_spray.sss");

    private final Component name;

    GraffitiSpray(String code) {
        this.name = TextUtil.text(Component.translatable(code));
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }
}
