package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Objects;

public abstract class AbstractCompetitionGUI extends PluginGUI {
    public AbstractCompetitionGUI(int size, Component title) {
        super(size, title);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        switch (Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer().get(ItemUtil.ITEM_ID, PersistentDataType.STRING))) {
            case "competition_info":
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                p.openInventory(new CompetitionInfoGUI().getInventory());
                break;
            case "competitor_list":
                p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                p.openInventory(new CompetitorListGUI().getInventory());
                break;
            case "competition_settings":
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                p.openInventory(new CompetitionSettingsGUI().getInventory());
                break;
            default:
                onClick(p, item);
                break;
        }
    }

    protected abstract void onClick(@NotNull Player p, @NotNull ItemStack item);
}
