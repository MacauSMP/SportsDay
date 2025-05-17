package org.macausmp.sportsday.gui.customize;

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
    private final PageBox<Material> pageBox = new PageBox<>(this, 1, 18,
            () -> Arrays.stream(DyeColor.values()).map(c -> Material.getMaterial(c.name() + "_DYE")).toList());
    private Material selected;

    public ClothingColorGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(27, Component.translatable("gui.customize.clothing.color.title"));
        this.slot = slot;
        DyeColor color = DyeColor.getByColor(Objects.requireNonNull(PlayerCustomize.getCloth(player, slot)).getColor());
        if (color != null)
            selected = Material.getMaterial(color.name() + "_DYE");
        for (int i = 18; i < 27; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(26, BACK);
        getInventory().setItem(0, reset());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::dye);
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

    @ButtonHandler("dye")
    public void dye(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        String s = item.getType().name();
        PlayerCustomize.setClothColor(p, slot, DyeColor.valueOf(s.substring(0, s.length() - 4)).getColor());
        selected = item.getType();
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothColor(p, slot, null);
        selected = null;
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ClothingCustomizeGUI(p).open(p);
    }
}
