package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;

public class ScheduleGUI extends PluginGUI {
    public ScheduleGUI() {
        super(54, Component.translatable("gui.menu.schedule.title"));
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new MenuGUI().open(p);
    }
}
