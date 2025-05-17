package org.macausmp.sportsday.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.function.Predicate;

/**
 * Represents a plugin gui.
 */
public abstract class PluginGUI implements InventoryHolder {
    private static final Map<Class<? extends PluginGUI>, Map<String, Method>> BUTTON_HANDLER = new HashMap<>();
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    protected static final ItemStack BOARD = ItemUtil.hideTooltip(ItemUtil.item(Material.BLACK_STAINED_GLASS_PANE, null, ""));
    protected static final ItemStack NEXT_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page", "gui.page.next");
    protected static final ItemStack PREVIOUS_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page", "gui.page.prev");
    protected static final ItemStack BACK = ItemUtil.item(Material.ARROW, "back", Component.translatable("gui.page.back"));
    protected static final Sound UI_BUTTON_CLICK_SOUND = Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f);
    protected static final Sound EXECUTION_SUCCESS_SOUND = Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f);
    protected static final Sound EXECUTION_FAIL_SOUND = Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f);
    protected static final Sound TELEPORT_SOUND = Sound.sound(Key.key("minecraft:entity.bat.takeoff"), Sound.Source.MASTER, 1f, 1f);
    private final boolean op;
    private final Inventory inventory;

    /**
     * A plugin gui with the specified size and title.
     *
     * @param size gui size
     * @param title gui title
     */
    public PluginGUI(int size, @NotNull Component title) {
        Class<? extends PluginGUI> clazz = getClass();
        if (!BUTTON_HANDLER.containsKey(clazz)) {
            BUTTON_HANDLER.put(clazz, new HashMap<>());
            for (Method method : clazz.getMethods()) {
                ButtonHandler handler = method.getAnnotation(ButtonHandler.class);
                if (handler != null)
                    BUTTON_HANDLER.get(clazz).putIfAbsent(handler.value(), method);
            }
        }
        this.op = clazz.getAnnotation(PermissionRequired.class) != null;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Get the gui {@link Inventory} content.
     *
     * @return gui {@link Inventory} content
     */
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Opens a gui to specified player.
     *
     * @param player The player to open the gui
     */
    public void open(@NotNull Player player) {
        update();
        player.openInventory(inventory);
        player.playSound(UI_BUTTON_CLICK_SOUND);
    }

    /**
     * Update gui content.
     */
    protected void update() {}

    /**
     * Update all gui content
     */
    protected void updateAll() {
        Class<? extends PluginGUI> clazz = getClass();
        PLUGIN.getServer().getOnlinePlayers().stream()
                .map(p -> p.getOpenInventory().getTopInventory().getHolder())
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .forEach(PluginGUI::update);
    }

    /**
     * Update all gui content with specify gui holder
     *
     * @param clazz class of gui holder
     */
    public static <T extends PluginGUI> void updateAll(@NotNull Class<T> clazz) {
        PLUGIN.getServer().getOnlinePlayers().stream()
                .map(p -> p.getOpenInventory().getTopInventory().getHolder())
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .forEach(T::update);
    }

    /**
     * Update all gui content with specify gui holder
     *
     * @param clazz class of gui holder
     * @param predicate predicate of gui
     */
    public static <T extends PluginGUI> void updateAll(@NotNull Class<T> clazz, Predicate<T> predicate) {
        PLUGIN.getServer().getOnlinePlayers().stream()
                .map(p -> p.getOpenInventory().getTopInventory().getHolder())
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(predicate)
                .forEach(T::update);
    }

    public final void click(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack item) {
        if (op && !player.isOp()) {
            player.sendMessage(Component.translatable("gui.permission.op").color(NamedTextColor.RED));
            return;
        }
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
