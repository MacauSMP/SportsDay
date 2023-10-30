package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

public abstract class AbstractCompetitionGUI extends PluginGUI {
    public AbstractCompetitionGUI(int size, Component title) {
        super(size, title);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.COMPETITION_INFO)) {
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            p.openInventory(new CompetitionInfoGUI().getInventory());
            return;
        } else if (ItemUtil.equals(item, GUIButton.COMPETITOR_LIST)) {
            p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
            p.openInventory(new CompetitorListGUI().getInventory());
            return;
        } else if (ItemUtil.equals(item, GUIButton.COMPETITION_SETTINGS)) {
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            p.openInventory(new CompetitionSettingsGUI().getInventory());
            return;
        }
        onClick(p, item);
    }

    protected abstract void onClick(@NotNull Player p, @NotNull ItemStack item);
}
