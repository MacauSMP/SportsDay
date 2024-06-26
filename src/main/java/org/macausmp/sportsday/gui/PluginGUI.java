package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a plugin gui.
 */
public abstract class PluginGUI implements InventoryHolder {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    protected static final ItemStack BOARD = ItemUtil.item(Material.BLACK_STAINED_GLASS_PANE, null, "");
    protected static final ItemStack NEXT_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page", "gui.page.next");
    protected static final ItemStack PREVIOUS_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page", "gui.page.prev");
    protected static final ItemStack BACK = ItemUtil.item(Material.ARROW, "back", Component.translatable("gui.page.back"));
    private static final Map<Class<? extends PluginGUI>, Map<String, Method>> BUTTON_HANDLER = new HashMap<>();
    private final Inventory inventory;

    /**
     * A plugin gui with the specified size and title.
     * @param size gui size
     * @param title gui title
     */
    public PluginGUI(int size, Component title) {
        if (!BUTTON_HANDLER.containsKey(getClass())) {
            BUTTON_HANDLER.put(getClass(), new HashMap<>());
            Method[] methods = getClass().getMethods();
            for (Method method : methods) {
                ButtonHandler handler = method.getAnnotation(ButtonHandler.class);
                if (handler != null && !BUTTON_HANDLER.get(getClass()).containsKey(handler.value()))
                    BUTTON_HANDLER.get(getClass()).put(handler.value(), method);
            }
        }
        inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Get the gui {@link Inventory} content.
     * @return gui {@link Inventory} content
     */
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Update gui content.
     */
    public void update() {}

    public final void click(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack item) {
        try {
            Map<String, Method> map = BUTTON_HANDLER.get(getClass());
            Method method = map.get("default");
            if (method != null)
                method.invoke(this, event, player, item);
            String id = ItemUtil.getID(item);
            if (id == null)
                return;
            method = map.get(id);
            if (method != null)
                method.invoke(this, event, player, item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
