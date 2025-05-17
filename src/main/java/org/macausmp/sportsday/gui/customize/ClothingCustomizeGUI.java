package org.macausmp.sportsday.gui.customize;

import com.google.common.collect.ImmutableMultimap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
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

    public ClothingCustomizeGUI(@NotNull Player player) {
        super(45, Component.translatable("gui.customize.clothing.title"));
        this.player = player;
        for (int i = 36; i < 45; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(44, BACK);
        getInventory().setItem(8, reset("gui.customize.clothing.head"));
        getInventory().setItem(17, reset("gui.customize.clothing.chest"));
        getInventory().setItem(26, reset("gui.customize.clothing.legs"));
        getInventory().setItem(35, reset("gui.customize.clothing.feet"));
        update();
    }

    @Override
    protected void update() {
        for (int i = 0; i < 4; i++) {
            PlayerCustomize.Cloth selected = PlayerCustomize.getCloth(player, EquipmentSlot.values()[5 - i]);
            for (int j = 0; j < 6; j++) {
                Material type = Material.getMaterial(MATERIAL[j] + "_" + ARMOR[i]);
                ItemStack stack = select(type);
                if (selected != null && selected.getMaterial().equals(type)) {
                    List<Component> lore = Objects.requireNonNull(stack.lore());
                    lore.set(0, TextUtil.text(Component.translatable("gui.selected")));
                    stack.lore(lore);
                    stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
                }
                getInventory().setItem(2 + i * 9 + j, stack);
            }
        }
        getInventory().setItem(0, present(EquipmentSlot.HEAD));
        getInventory().setItem(9, present(EquipmentSlot.CHEST));
        getInventory().setItem(18, present(EquipmentSlot.LEGS));
        getInventory().setItem(27, present(EquipmentSlot.FEET));
    }

    private @Nullable ItemStack present(@NotNull EquipmentSlot slot) {
        PlayerCustomize.Cloth cloth = PlayerCustomize.getCloth(player, slot);
        if (cloth == null)
            return null;
        ItemStack stack = PlayerCustomize.getClothItemStack(cloth);
        stack.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.trim.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "present");
        });
        return stack;
    }

    private @NotNull ItemStack select(Material material) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            if (stack.getItemMeta() instanceof ColorableArmorMeta)
                lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.color.lore")));
            meta.lore(lore);
            meta.setAttributeModifiers(ImmutableMultimap.of());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "select_cloth");
        });
        PlayerCustomize.Cloth cloth = PlayerCustomize.getCloth(player, material.getEquipmentSlot());
        if (stack.getItemMeta() instanceof ColorableArmorMeta && cloth != null)
            stack.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(cloth.getColor()));
        return stack;
    }

    private @NotNull ItemStack reset(String slot) {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset")
                .arguments(Component.translatable(slot)));
    }

    @ButtonHandler("present")
    public void present(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (e.isRightClick()) {
            new ClothingTrimGUI(p, item.getType().getEquipmentSlot()).open(p);
        }
    }

    @ButtonHandler("select_cloth")
    public void selectCloth(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (e.isLeftClick()) {
            PlayerCustomize.setClothMaterial(p, item.getType());
            update();
            PlayerCustomize.suitUp(p);
            p.playSound(Sound.sound(Key.key("minecraft:item.armor.equip_generic"), Sound.Source.MASTER, 1f, 1f));
        } else if (e.isRightClick() && item.getItemMeta() instanceof ColorableArmorMeta) {
            if (PlayerCustomize.getCloth(p, item.getType().getEquipmentSlot()) == null)
                PlayerCustomize.setClothMaterial(p, item.getType());
            new ClothingColorGUI(p, item.getType().getEquipmentSlot()).open(p);
        }
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        switch (e.getSlot()) {
            case 8 -> PlayerCustomize.resetCloth(p, EquipmentSlot.HEAD);
            case 17 -> PlayerCustomize.resetCloth(p, EquipmentSlot.CHEST);
            case 26 -> PlayerCustomize.resetCloth(p, EquipmentSlot.LEGS);
            case 35 -> PlayerCustomize.resetCloth(p, EquipmentSlot.FEET);
        }
        p.playSound(UI_BUTTON_CLICK_SOUND);
        update();
        PlayerCustomize.suitUp(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
