package org.macausmp.sportsday.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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
    private static final ItemStack CONFIRM = ItemUtil.item(Material.GREEN_CONCRETE, "confirm", Component.translatable("gui.confirm.confirm").color(NamedTextColor.GREEN));
    private static final ItemStack CANCEL = ItemUtil.item(Material.RED_CONCRETE, "cancel", Component.translatable("gui.confirm.cancel").color(NamedTextColor.RED));
    private final PluginGUI prev;
    private final Function<Player, Boolean> function;

    /**
     * Construct a {@code ConfirmGUI} with previous gui and a function.
     * <p>If the function returns {@code True} then close the inventory, otherwise fall back to the previous gui.</p>
     * @param prev previous gui
     * @param function function executed after confirmation
     */
    public ConfirmationGUI(PluginGUI prev, Function<Player, Boolean> function) {
        super(27, Component.translatable("gui.confirm.title"));
        this.prev = prev;
        this.function = function;
        getInventory().setItem(11, CONFIRM);
        getInventory().setItem(15, CANCEL);
    }

    @ButtonHandler("confirm")
    public void confirm(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        if (!function.apply(p)) {
            p.openInventory(prev.getInventory());
            prev.update();
        } else {
            p.closeInventory();
        }
    }

    @ButtonHandler("cancel")
    public void cancel(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(prev.getInventory());
        prev.update();
    }
}
