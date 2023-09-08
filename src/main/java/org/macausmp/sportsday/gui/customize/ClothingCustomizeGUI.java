package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClothingCustomizeGUI extends AbstractGUI {
    private static final String[] MATERIAL = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
    private static final String[] ARMOR = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};

    public ClothingCustomizeGUI(Player player) {
        super(45, Component.translatable("gui.customize.clothing.title"), player);
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(17, reset("gui.customize.clothing.head"));
        getInventory().setItem(26, reset("gui.customize.clothing.chest"));
        getInventory().setItem(35, reset("gui.customize.clothing.legs"));
        getInventory().setItem(44, reset("gui.customize.clothing.feet"));
    }

    @Override
    public void update() {
        for (int i = 0; i < 4; i++) {
            ItemStack selected = PlayerCustomize.getClothItem(player, EquipmentSlot.values()[5 - i]);
            for (int j = 0; j < 6; j++) {
                Material type = Material.getMaterial(MATERIAL[j] + "_" + ARMOR[i]);
                ItemStack stack = select(type);
                if (selected != null && selected.getType().equals(type)) {
                    List<Component> lore = Objects.requireNonNull(stack.lore());
                    lore.set(0, TextUtil.text(Component.translatable("gui.selected")));
                    stack.lore(lore);
                    stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                }
                getInventory().setItem(11 + i * 9 + j, stack);
            }
        }
        getInventory().setItem(9, present(EquipmentSlot.HEAD));
        getInventory().setItem(18, present(EquipmentSlot.CHEST));
        getInventory().setItem(27, present(EquipmentSlot.LEGS));
        getInventory().setItem(36, present(EquipmentSlot.FEET));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (item.getType().getEquipmentSlot().isArmor()) {
            if (ItemUtil.equals(item, "select_cloth")) {
                if (e.isLeftClick()) {
                    PlayerCustomize.setClothItem(p, item.getType());
                } else if (e.isRightClick() && item.getItemMeta() instanceof ColorableArmorMeta) {
                    p.openInventory(new ClothingColorGUI(p, item.getType().getEquipmentSlot()).getInventory());
                    p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                    return;
                }
            } else if (ItemUtil.equals(item, "cloth") && e.isRightClick()) {
                p.openInventory(new ClothingTrimGUI(p, item.getType().getEquipmentSlot()).getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            } else {
                return;
            }
        } else {
            switch (e.getSlot()) {
                case 17 -> PlayerCustomize.resetCloth(p, EquipmentSlot.HEAD);
                case 26 -> PlayerCustomize.resetCloth(p, EquipmentSlot.CHEST);
                case 35 -> PlayerCustomize.resetCloth(p, EquipmentSlot.LEGS);
                case 44 -> PlayerCustomize.resetCloth(p, EquipmentSlot.FEET);
            }
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    private @Nullable ItemStack present(@NotNull EquipmentSlot slot) {
        ItemStack cloth = PlayerCustomize.getClothItem(player, slot);
        if (cloth == null) return null;
        cloth.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.trim.lore")));
            meta.lore(lore);
            if (PlayerCustomize.hasClothTrim(player, slot)) meta.setTrim(new ArmorTrim(PlayerCustomize.getClothTrimMaterial(player, slot), PlayerCustomize.getClothTrimPattern(player, slot)));
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "cloth");
        });
        if (cloth.getItemMeta() instanceof ColorableArmorMeta) {
            cloth.editMeta(ColorableArmorMeta.class, meta -> {
                Color color = PlayerCustomize.getClothColor(player, slot);
                meta.setColor(color);
            });
        }
        return cloth;
    }

    private @NotNull ItemStack select(Material material) {
        ItemStack cloth = new ItemStack(material);
        cloth.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            if (cloth.getItemMeta() instanceof ColorableArmorMeta) lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.color.lore")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "select_cloth");
        });
        if (cloth.getItemMeta() instanceof ColorableArmorMeta) {
            Color color = PlayerCustomize.getClothColor(player, material.getEquipmentSlot());
            cloth.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(color));
        }
        return cloth;
    }

    private @NotNull ItemStack reset(String slot) {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset").args(Component.translatable(slot)));
    }
}
