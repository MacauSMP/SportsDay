package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

public abstract class AbstractGUI implements IPluginGUI {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private final Inventory inventory;
    protected final Player player;

    public AbstractGUI(int size, Component title, Player player) {
        inventory = Bukkit.createInventory(this, size, title);
        this.player = player;
        update();
    }

    public AbstractGUI(int size, Component title) {
        this(size, title, null);
    }

    /**
     * Get the GUI {@link Inventory} content
     * @return GUI {@link Inventory} content
     */
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }
}
