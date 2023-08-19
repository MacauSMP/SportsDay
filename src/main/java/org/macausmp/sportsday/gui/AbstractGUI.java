package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

public abstract class AbstractGUI implements IPluginGUI {
    protected final SportsDay plugin = SportsDay.getInstance();
    private final Inventory inventory;

    public AbstractGUI(int size, Component title) {
        inventory = Bukkit.createInventory(null, size, title);
        update();
    }

    @Override
    public final Inventory getInventory() {
        return inventory;
    }

    @Override
    public void openTo(@NotNull Player player) {
        player.openInventory(inventory);
        GUIManager.GUI_MAP.put(player, this);
    }
}
