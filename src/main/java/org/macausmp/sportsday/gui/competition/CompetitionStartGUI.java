package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.FieldEvent;
import org.macausmp.sportsday.competition.SportingEvent;
import org.macausmp.sportsday.competition.TrackEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionStartGUI extends AbstractCompetitionGUI {
    public CompetitionStartGUI() {
        super(54, Component.translatable("gui.start.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, start(Competitions.ELYTRA_RACING));
        getInventory().setItem(19, start(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(20, start(Competitions.JAVELIN_THROW));
        getInventory().setItem(21, start(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(22, start(Competitions.PARKOUR));
        getInventory().setItem(23, start(Competitions.SUMO));
    }

    @ButtonHandler("event_start")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!Competitions.start(p, item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE)))
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack start(@NotNull SportingEvent event) {
        ItemStack stack = ItemUtil.item(event.getDisplayItem(), "event_start", event.getName());
        stack.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            if (event instanceof TrackEvent trackEvent) {
                lore.add(Component.translatable("event.type.track").color(NamedTextColor.GRAY));
                lore.add(Component.translatable("event.track.laps")
                        .arguments(Component.text(trackEvent.getMaxLaps()))
                        .color(NamedTextColor.YELLOW));
            } else if (event instanceof FieldEvent) {
                lore.add(Component.translatable("event.type.field").color(NamedTextColor.GRAY));
            }
            lore.add(Component.translatable("gui.start_competition"));
            meta.lore(lore.stream().map(TextUtil::text).toList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE, event.getKey());
        });
        return stack;
    }
}
