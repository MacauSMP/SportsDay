package org.macausmp.sportsday.customize;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.util.TextUtil;

public enum CustomizeGraffitiSpray {
    GGWP("customize.graffiti_spray.ggwp", 0),
    DISMAL("customize.graffiti_spray.d", 1),
    CRAZY("customize.graffiti_spray.c", 2),
    BADASS("customize.graffiti_spray.b", 3),
    APOCALYPTIC("customize.graffiti_spray.a", 4),
    SAVAGE("customize.graffiti_spray.s", 5),
    SICK_SKILLS("customize.graffiti_spray.ss", 6),
    SMOKIN_SEXY_STYLE("customize.graffiti_spray.sss", 7),
    ;

    private final Component name;
    private final int id;

    CustomizeGraffitiSpray(String code, int id) {
        this.name = TextUtil.text(Component.translatable(code));
        this.id = id;
    }

    public Component getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
