package org.macausmp.sportsday.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.TextUtil;

public enum Musickit implements Keyed, ComponentLike {
    TAKE_ON_ME("customize.musickit.take_on_me", "take_on_me"),
    BAKA_MITAI("customize.musickit.baka_mitai", "baka_mitai"),
    KABUTACK("customize.musickit.kabutack", "kabutack"),
    VIRTUAL_INSANITY("customize.musickit.virtual_insanity", "virtual_insanity"),
    CANT_TOUCH_THIS("customize.musickit.cant_touch_this", "cant_touch_this"),
    ERIKA("customize.musickit.erika", "erika"),
    YOU_SPIN_ME_ROUND("customize.musickit.you_spin_me_round", "you_spin_me_round"),
    BONES("customize.musickit.bones", "bones"),
    MAPS("customize.musickit.maps", "maps"),
    TURN_DOWN_FOR_WHAT("customize.musickit.turn_down_for_what", "turn_down_for_what"),
    THE_GOOD_YOUTH("customize.musickit.the_good_youth", "the_good_youth"),
    FLASHBANG_DANCE("customize.musickit.flashbang_dance", "flashbang_dance"),
    SUBHUMAN("customize.musickit.subhuman", "subhuman"),
    BURY_THE_LIGHT("customize.musickit.bury_the_light", "bury_the_light"),
    DEVIL_TRIGGER("customize.musickit.devil_trigger", "devil_trigger");

    private final Component name;
    private final Key key;

    Musickit(String code, @KeyPattern.Value String key) {
        this.name = TextUtil.text(Component.translatable(code));
        this.key = Key.key(Key.MINECRAFT_NAMESPACE, "mvp." + key);
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
