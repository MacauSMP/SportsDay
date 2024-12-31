package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class TextUtil {
    /**
     * Convert legacy color code '§' to {@link net.kyori.adventure.text.format.TextColor}.
     * @param origin origin component
     * @return component with color and style
     */
    public static @NotNull Component convert(@NotNull ComponentLike origin) {
        return LegacyComponentSerializer.legacySection()
                .deserialize(LegacyComponentSerializer.legacySection().serialize(origin.asComponent()))
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert {@link ComponentLike} to plain text {@link Component}.
     * @param like component
     * @return plain text component
     */
    public static @NotNull Component text(@NotNull ComponentLike like) {
        return Component.text(LegacyComponentSerializer.legacySection().serialize(like.asComponent()))
                .decoration(TextDecoration.ITALIC, false);
    }
}
