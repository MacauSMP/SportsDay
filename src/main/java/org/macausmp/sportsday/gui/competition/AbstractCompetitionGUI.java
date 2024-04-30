package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;

public abstract class AbstractCompetitionGUI extends PluginGUI {
    public AbstractCompetitionGUI(int size, Component title) {
        super(size, title);
    }

    @ButtonHandler("competition_info")
    public void info(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitionInfoGUI().getInventory());
    }

    @ButtonHandler("competitor_list")
    public void list(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitorListGUI().getInventory());
    }

    @ButtonHandler("competition_settings")
    public void settings(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitionSettingsGUI().getInventory());
    }
}
