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
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClothingCustomizeGUI extends PluginGUI {
    private static final String[] MATERIAL = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
    private static final String[] ARMOR = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private final Player player;

    public ClothingCustomizeGUI(Player player) {
        super(45, Component.translatable("gui.customize.clothing.title"));
        this.player = player;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(17, reset("gui.customize.clothing.head"));
        getInventory().setItem(26, reset("gui.customize.clothing.chest"));
        getInventory().setItem(35, reset("gui.customize.clothing.legs"));
        getInventory().setItem(44, reset("gui.customize.clothing.feet"));
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < 4; i++) {
            PlayerCustomize.Cloth selected = PlayerCustomize.getCloth(player, EquipmentSlot.values()[5 - i]);
            for (int j = 0; j < 6; j++) {
                Material type = Material.getMaterial(MATERIAL[j] + "_" + ARMOR[i]);
                ItemStack stack = select(type);
                if (selected != null && selected.getMaterial().equals(type)) {
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

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("select_cloth")
    public void selectCloth(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (e.isLeftClick()) {
            PlayerCustomize.setClothMaterial(p, item.getType());
            update();
            PlayerCustomize.suitUp(p);
        } else if (e.isRightClick() && item.getItemMeta() instanceof ColorableArmorMeta) {
            p.openInventory(new ClothingColorGUI(p, item.getType().getEquipmentSlot()).getInventory());
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("cloth")
    public void cloth(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (e.isRightClick()) {
            p.openInventory(new ClothingTrimGUI(p, item.getType().getEquipmentSlot()).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        }
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        switch (e.getSlot()) {
            case 17 -> PlayerCustomize.resetCloth(p, EquipmentSlot.HEAD);
            case 26 -> PlayerCustomize.resetCloth(p, EquipmentSlot.CHEST);
            case 35 -> PlayerCustomize.resetCloth(p, EquipmentSlot.LEGS);
            case 44 -> PlayerCustomize.resetCloth(p, EquipmentSlot.FEET);
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    private @Nullable ItemStack present(@NotNull EquipmentSlot slot) {
        PlayerCustomize.Cloth cloth = PlayerCustomize.getCloth(player, slot);
        if (cloth == null)
            return null;
        ItemStack item = PlayerCustomize.getClothItemStack(cloth);
        item.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.trim.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "cloth");
        });
        return item;
    }

    private @NotNull ItemStack select(Material material) {
        ItemStack cloth = new ItemStack(material);
        cloth.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            if (cloth.getItemMeta() instanceof ColorableArmorMeta)
                lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.color.lore")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "select_cloth");
        });
        PlayerCustomize.Cloth cloth1 = PlayerCustomize.getCloth(player, material.getEquipmentSlot());
        if (cloth.getItemMeta() instanceof ColorableArmorMeta && cloth1 != null)
            cloth.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(cloth1.getColor()));
        return cloth;
    }

    private @NotNull ItemStack reset(String slot) {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset")
                .args(Component.translatable(slot)));
    }
}
