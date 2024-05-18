package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ClothingTrimGUI extends PluginGUI {
    private final Player player;
    private final EquipmentSlot slot;

    public ClothingTrimGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(54, Component.translatable("gui.customize.clothing.trim.title"));
        this.player = player;
        this.slot = slot;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(9, material(Material.QUARTZ));
        getInventory().setItem(17, material(Material.IRON_INGOT));
        getInventory().setItem(18, material(Material.NETHERITE_INGOT));
        getInventory().setItem(26, material(Material.REDSTONE));
        getInventory().setItem(27, material(Material.COPPER_INGOT));
        getInventory().setItem(35, material(Material.GOLD_INGOT));
        getInventory().setItem(36, material(Material.EMERALD));
        getInventory().setItem(44, material(Material.DIAMOND));
        getInventory().setItem(45, material(Material.LAPIS_LAZULI));
        getInventory().setItem(53, material(Material.AMETHYST_SHARD));
        getInventory().setItem(22, present());
        getInventory().setItem(40, reset());
        List<Material> patterns = new ArrayList<>();
        for (Field field : TrimPattern.class.getFields())
            patterns.add(Material.getMaterial(field.getName() + "_ARMOR_TRIM_SMITHING_TEMPLATE"));
        Iterator<Material> iterator = patterns.iterator();
        for (int i = 10; i < 44; i++) {
            int j = i % 9;
            if (j == 1 || j == 2 || j == 6 || j == 7)
                getInventory().setItem(i, pattern(iterator.next()));
        }
        PlayerCustomize.Cloth cloth = Objects.requireNonNull(PlayerCustomize.getCloth(player, slot));
        String material = cloth.getTrimMaterial();
        if (!material.equals(PlayerCustomize.Cloth.NONE))
            for (int i = 9; i < 54; i++) {
                ItemStack material2 = getInventory().getItem(i);
                if (material2 != null && material2.getType().name().equals(material)) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(TextUtil.text(Component.translatable("gui.selected")));
                    material2.lore(lore);
                    material2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    material2.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                    break;
                }
            }
        String pattern = cloth.getTrimPattern();
        if (!pattern.equals(PlayerCustomize.Cloth.NONE))
            for (int i = 10; i < 45; i++) {
                ItemStack pattern2 = getInventory().getItem(i);
                if (pattern2 != null && pattern2.getType().name().startsWith(pattern)) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(TextUtil.text(Component.translatable("gui.selected")));
                    pattern2.lore(lore);
                    pattern2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    pattern2.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                    break;
                }
            }
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new ClothingCustomizeGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("select_material")
    public void selectMaterial(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothTrimMaterial(p, slot, item.getType());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("select_pattern")
    public void selectPattern(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setClothTrimPattern(p, slot, item.getType());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.resetClothTrim(p, slot);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    private @NotNull ItemStack material(Material material) {
        return ItemUtil.item(material, "select_material", "gui.select");
    }

    private @NotNull ItemStack pattern(Material material) {
        return ItemUtil.item(material, "select_pattern", "gui.select");
    }

    private @NotNull ItemStack present() {
        return PlayerCustomize.getClothItemStack(Objects.requireNonNull(PlayerCustomize.getCloth(player, slot)));
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset")
                .args(Component.translatable("gui.customize.clothing.reset_trim")));
    }
}
