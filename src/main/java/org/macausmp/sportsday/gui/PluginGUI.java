package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.CompetitionListener;
import org.macausmp.sportsday.SportsDay;

/**
 * Represents a plugin gui
 */
public abstract class PluginGUI implements InventoryHolder {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private final Inventory inventory;

    public PluginGUI(int size, Component title) {
        inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Get the GUI {@link Inventory} content
     * @return GUI {@link Inventory} content
     */
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Update GUI content
     */
    public abstract void update();

    /**
     * Listener call from {@link CompetitionListener#onClick(InventoryClickEvent)}
     * @param event event given from {@link CompetitionListener#onClick(InventoryClickEvent)}
     */
    public abstract void onClick(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack item);
}
