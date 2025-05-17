package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.function.Function;

/**
 * Represents a confirmation gui.
 */
public class ConfirmationGUI extends PluginGUI {
    private static final ItemStack CONFIRM = ItemUtil.item(Material.YELLOW_CONCRETE, "confirm", Component.translatable("gui.confirm.confirm").color(NamedTextColor.GREEN));
    private static final ItemStack CANCEL = ItemUtil.item(Material.BARRIER, "cancel", Component.translatable("gui.confirm.cancel").color(NamedTextColor.RED));
    private final PluginGUI prev;
    private final Function<Player, PluginGUI> function;

    /**
     * Construct a {@code ConfirmGUI} with previous gui and a function.
     *
     * <p>When the confirm button is clicked, open the GUI from the function</p>
     *
     * @param prev previous gui
     * @param function function executed after confirmation
     */
    public ConfirmationGUI(@NotNull PluginGUI prev, @NotNull Function<Player, PluginGUI> function) {
        super(27, Component.translatable("gui.confirm.title"));
        this.prev = prev;
        this.function = function;
        getInventory().setItem(11, CONFIRM);
        getInventory().setItem(15, CANCEL);
    }

    @ButtonHandler("confirm")
    public void confirm(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        PluginGUI gui = function.apply(p);
        if (gui == null) {
            p.closeInventory();
            return;
        }
        gui.update();
        p.openInventory(gui.getInventory());
    }

    @ButtonHandler("cancel")
    public void cancel(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        prev.update();
        p.openInventory(prev.getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
