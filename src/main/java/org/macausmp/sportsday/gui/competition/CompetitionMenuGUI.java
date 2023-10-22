package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.GUIButton;

public class CompetitionMenuGUI extends PluginGUI {
    public CompetitionMenuGUI() {
        super(27, Component.translatable("gui.menu.title"));
        getInventory().setItem(10, GUIButton.COMPETITION_INFO);
        getInventory().setItem(12, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(14, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(16, GUIButton.VERSION);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, Player p, ItemStack item) {
    }
}
