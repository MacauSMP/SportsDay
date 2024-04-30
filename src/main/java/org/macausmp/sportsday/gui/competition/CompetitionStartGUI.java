package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;

public class CompetitionStartGUI extends AbstractCompetitionGUI {
    public CompetitionStartGUI() {
        super(54, Component.translatable("gui.start.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(27, start(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, start(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, start(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, start(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, start(Competitions.PARKOUR));
        getInventory().setItem(32, start(Competitions.SUMO));
    }

    @ButtonHandler("start_competition")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        boolean b = Competitions.start(p, item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING));
        if (!b) p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack start(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.head(ItemUtil.START, "start_competition", "gui.start_competition");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID()));
        return stack;
    }
}
