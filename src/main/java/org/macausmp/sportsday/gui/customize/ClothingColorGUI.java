package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Arrays;
import java.util.Objects;

public class ClothingColorGUI extends PluginGUI {
    private final EquipmentSlot slot;
    private final PageBox<Material> pageBox = new PageBox<>(this, 10, 27,
            () -> Arrays.stream(DyeColor.values()).map(c -> Material.getMaterial(c.name() + "_DYE")).toList());
    private Material selected;

    public ClothingColorGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(27, Component.translatable("gui.customize.clothing.color.title"));
        this.slot = slot;
        DyeColor color = DyeColor.getByColor(Objects.requireNonNull(PlayerCustomize.getCloth(player, slot)).getColor());
        if (color != null)
            selected = Material.getMaterial(color.name() + "_DYE");
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        pageBox.updatePage(this::dye);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new ClothingCustomizeGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("dye")
    public void dye(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        String s = item.getType().name();
        PlayerCustomize.setClothColor(p, slot, DyeColor.valueOf(s.substring(0, s.length() - 4)).getColor());
        selected = item.getType();
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothColor(p, slot, null);
        selected = null;
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    private @NotNull ItemStack dye(Material material) {
        ItemStack stack = ItemUtil.item(material, "dye", material == selected ? "gui.selected" : "gui.select");
        if (material == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset")
                .arguments(Component.translatable("gui.customize.clothing.reset_color")));
    }
}
