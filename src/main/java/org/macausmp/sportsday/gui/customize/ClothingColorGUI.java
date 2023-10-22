package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.Objects;

public class ClothingColorGUI extends PluginGUI {
    private static final int START_INDEX = 10;
    private final Player player;
    private final EquipmentSlot slot;

    public ClothingColorGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(27, Component.translatable("gui.customize.clothing.color.title"));
        this.player = player;
        this.slot = slot;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < DyeColor.values().length; i++) {
            getInventory().setItem(i + START_INDEX, dye(Material.getMaterial(DyeColor.values()[i].name() + "_DYE")));
        }
        for (int i = START_INDEX; i < START_INDEX + DyeColor.values().length; i++) {
            ItemStack dye = getInventory().getItem(i);
            Color color = PlayerCustomize.getClothColor(player, slot);
            if (color == null || DyeColor.getByColor(color) == null) return;
            if (dye == null) break;
            if (dye.getType().name().equals(Objects.requireNonNull(DyeColor.getByColor(color)).name() + "_DYE")) {
                dye.editMeta(meta -> meta.displayName(TextUtil.text(Component.translatable("gui.selected"))));
                dye.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                dye.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.BACK)) {
            p.openInventory(new ClothingCustomizeGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        String s = item.getType().name();
        if (s.endsWith("_DYE")) {
            PlayerCustomize.setClothColor(p, slot, DyeColor.valueOf(s.substring(0, s.length() - 4)).getColor());
        } else if (ItemUtil.equals(item, reset())) {
            PlayerCustomize.setClothColor(p, slot, null);
        }
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
        PlayerCustomize.suitUp(p);
    }

    private @NotNull ItemStack dye(Material material) {
        return ItemUtil.item(material, "dye", "gui.select");
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", Component.translatable("gui.customize.clothing.reset").args(Component.translatable("gui.customize.clothing.reset_color")));
    }
}
