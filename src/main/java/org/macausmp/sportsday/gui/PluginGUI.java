package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.CompetitionListener;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a plugin gui
 */
public abstract class PluginGUI implements InventoryHolder {
    private static final Map<Class<? extends PluginGUI>, Map<String, Method>> BUTTON_HANDLER = new HashMap<>();
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private final Inventory inventory;

    /**
     * A plugin gui with the specified size and title
     * @param size gui size
     * @param title gui title
     */
    public PluginGUI(int size, Component title) {
        Class<? extends PluginGUI> clazz = this.getClass();
        if (!BUTTON_HANDLER.containsKey(clazz)) {
            BUTTON_HANDLER.put(clazz, new HashMap<>());
            Method[] methods = this.getClass().getMethods();
            for (Method method : methods) {
                ButtonHandler handler = method.getAnnotation(ButtonHandler.class);
                if (handler != null && !BUTTON_HANDLER.get(clazz).containsKey(handler.value())) {
                    BUTTON_HANDLER.get(clazz).put(handler.value(), method);
                }
            }
        }
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
    public void update() {}

    public final void click(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack item) {
        try {
            Map<String, Method> map = BUTTON_HANDLER.get(this.getClass());
            Method method = map.get("default");
            if (method != null) method.invoke(this, event, player, item);
            String id = ItemUtil.getID(item);
            if (id != null) {
                method = map.get(id);
                if (method != null) method.invoke(this, event, player, item);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Listener call from {@link CompetitionListener#onClose(InventoryCloseEvent)}
     */
    public void onClose() {}
}
