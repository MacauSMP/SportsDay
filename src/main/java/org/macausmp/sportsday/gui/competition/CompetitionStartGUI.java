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
import org.macausmp.sportsday.util.ItemUtil;

public class CompetitionStartGUI extends AbstractCompetitionGUI {
    public CompetitionStartGUI() {
        super(54, Component.translatable("gui.start.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, ELYTRA_RACING);
        getInventory().setItem(19, ICE_BOAT_RACING);
        getInventory().setItem(20, JAVELIN_THROW);
        getInventory().setItem(21, OBSTACLE_COURSE);
        getInventory().setItem(22, PARKOUR);
        getInventory().setItem(23, SUMO);
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
        if (!Competitions.start(p, item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING)))
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack start(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.head(ItemUtil.START, "start_competition", "gui.start_competition");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID()));
        return stack;
    }
}
