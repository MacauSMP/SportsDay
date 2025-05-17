package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Iterator;
import java.util.Objects;

public class ClothingTrimGUI extends PluginGUI {
    private final Player player;
    private final EquipmentSlot slot;
    private Material trimMaterial;
    private Material trimPattern;

    public ClothingTrimGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(54, Component.translatable("gui.customize.clothing.trim.title"));
        this.player = player;
        this.slot = slot;
        PlayerCustomize.Cloth cloth = Objects.requireNonNull(PlayerCustomize.getCloth(player, slot));
        trimMaterial = Material.getMaterial(cloth.getTrimMaterial());
        trimPattern = Material.getMaterial(cloth.getTrimPattern() + "_ARMOR_TRIM_SMITHING_TEMPLATE");
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        update();
    }

    @Override
    protected void update() {
        Iterator<Material> materials = PlayerCustomize.getTrimMaterial().iterator();
        for (int i = 0; i < 45; i++) {
            int j = i % 9;
            if (j == 0 || j == 8)
                getInventory().setItem(i, material(materials.next()));
        }
        Iterator<Material> patterns = PlayerCustomize.getTrimPattern().iterator();
        for (int i = 1; i < 35; i++) {
            int j = i % 9;
            if (j == 1 || j == 2 || j == 6 || j == 7)
                getInventory().setItem(i, pattern(patterns.next()));
        }
        getInventory().setItem(13, present());
        getInventory().setItem(31, reset());
    }

    private @NotNull ItemStack material(Material material) {
        ItemStack stack = ItemUtil.item(material, "select_material", material == trimMaterial ? "gui.selected" : "gui.select");
        if (material == trimMaterial)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack pattern(Material material) {
        ItemStack stack = ItemUtil.item(material, "select_pattern", material == trimPattern ? "gui.selected" : "gui.select");
        if (material == trimPattern)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack present() {
        return PlayerCustomize.getClothItemStack(Objects.requireNonNull(PlayerCustomize.getCloth(player, slot)));
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset")
                .arguments(Component.translatable("gui.customize.clothing.reset_trim")));
    }

    @ButtonHandler("select_material")
    public void selectMaterial(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothTrimMaterial(p, slot, trimMaterial = item.getType());
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("select_pattern")
    public void selectPattern(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothTrimPattern(p, slot, trimPattern = item.getType());
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.resetClothTrim(p, slot);
        trimMaterial = null;
        trimPattern = null;
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ClothingCustomizeGUI(p).open(p);
    }
}
