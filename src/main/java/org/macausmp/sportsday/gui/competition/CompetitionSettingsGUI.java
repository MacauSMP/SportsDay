package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.competition.setting.EventSettingsGUI;
import org.macausmp.sportsday.gui.competition.setting.SumoSettingsGUI;
import org.macausmp.sportsday.gui.competition.setting.TrackEventSettingsGUI;
import org.macausmp.sportsday.util.ItemUtil;

public class CompetitionSettingsGUI extends AbstractCompetitionGUI {
    public CompetitionSettingsGUI() {
        super(54, Component.translatable("gui.settings.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, ItemUtil.setGlint(COMPETITION_SETTINGS));
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, setting(Competitions.ELYTRA_RACING));
        getInventory().setItem(19, setting(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(20, setting(Competitions.JAVELIN_THROW));
        getInventory().setItem(21, setting(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(22, setting(Competitions.PARKOUR));
        getInventory().setItem(23, setting(Competitions.SUMO));
        update();
    }

    @ButtonHandler("event_setting")
    public void setting(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        IEvent event = Competitions.EVENTS.get(item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING));
        if (event == null)
            return;
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        if (event == Competitions.JAVELIN_THROW)
            p.openInventory(new EventSettingsGUI<>((JavelinThrow) event).getInventory());
        else if (event == Competitions.SUMO)
            p.openInventory(new SumoSettingsGUI((Sumo) event).getInventory());
        else if (event instanceof ITrackEvent track)
            p.openInventory(new TrackEventSettingsGUI(track).getInventory());
    }

    private @NotNull ItemStack setting(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.item(event.getDisplayItem(), "event_setting",
                Component.translatable("gui.event_settings.title").color(NamedTextColor.YELLOW).arguments(event.getName()),
                Component.translatable(event instanceof ITrackEvent ? "event.type.track" : "event.type.field").color(NamedTextColor.GRAY),
                Component.translatable("gui.event_settings.lore").arguments(event.getName()));
        stack.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID());
        });
        return stack;
    }
}
