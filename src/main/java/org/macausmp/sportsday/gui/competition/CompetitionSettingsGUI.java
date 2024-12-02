package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.competition.ITrackEvent;
import org.macausmp.sportsday.competition.JavelinThrow;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.competition.setting.EventSettingsGUI;
import org.macausmp.sportsday.gui.competition.setting.SumoSettingsGUI;
import org.macausmp.sportsday.gui.competition.setting.TrackEventSettingsGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionSettingsGUI extends AbstractCompetitionGUI {
    public CompetitionSettingsGUI() {
        super(54, Component.translatable("gui.settings.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, ItemUtil.setGlint(COMPETITION_SETTINGS));
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, setting(ELYTRA_RACING));
        getInventory().setItem(19, setting(ICE_BOAT_RACING));
        getInventory().setItem(20, setting(JAVELIN_THROW));
        getInventory().setItem(21, setting(OBSTACLE_COURSE));
        getInventory().setItem(22, setting(PARKOUR));
        getInventory().setItem(23, setting(SUMO));
        update();
    }

    @ButtonHandler("event")
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

    private @NotNull ItemStack setting(@NotNull ItemStack event) {
        ItemStack clone = event.clone();
        Component name = Competitions.EVENTS.get(clone.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING)).getName();
        clone.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.event_settings.title").color(NamedTextColor.YELLOW).arguments(name)));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.event_settings.lore").arguments(name)));
            meta.lore(lore);
        });
        return clone;
    }
}
