package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class WeaponSkinGUI extends AbstractGUI {
    public WeaponSkinGUI(Player player) {
        super(18, Component.translatable("gui.customize.weapon_skin.title"), player);
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
    }

    @Override
    public void update() {
        getInventory().setItem(9, weapon(Material.BLAZE_ROD));
        getInventory().setItem(10, weapon(Material.BONE));
        getInventory().setItem(11, weapon(Material.SHEARS));
        getInventory().setItem(12, weapon(Material.BAMBOO));
        getInventory().setItem(13, weapon(Material.DEAD_BUSH));
        getInventory().setItem(14, weapon(Material.SUGAR_CANE));
        getInventory().setItem(15, weapon(Material.COD));
        Material weapon = PlayerCustomize.getWeaponSkin(player);
        if (weapon == null) return;
        for (int i = 9; i < 17; i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            if (weapon.equals(stack.getType())) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("gui.selected")));
                stack.lore(lore);
                stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(item, "weapon")) {
            PlayerCustomize.setWeaponSkin(p, item.getType());
            p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            update();
        }
    }

    private @NotNull ItemStack weapon(Material material) {
        return ItemUtil.item(material, "weapon", null, "gui.select");
    }
}
