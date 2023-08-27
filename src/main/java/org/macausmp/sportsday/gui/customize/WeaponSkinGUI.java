package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class WeaponSkinGUI extends AbstractGUI {
    private final Player player;

    public WeaponSkinGUI(Player player) {
        super(18, Component.translatable("gui.customize.weapon_skin.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(9, weapon(Material.BLAZE_ROD));
        getInventory().setItem(10, weapon(Material.BONE));
        getInventory().setItem(11, weapon(Material.SHEARS));
        getInventory().setItem(12, weapon(Material.BAMBOO));
        getInventory().setItem(13, weapon(Material.DEAD_BUSH));
        getInventory().setItem(14, weapon(Material.COD));
        if (player == null) return;
        Material weapon = PlayerCustomize.getWeaponSkin(player);
        if (weapon == null) return;
        for (int i = 9; i < 17; i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            if (weapon.equals(stack.getType())) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("gui.selected")));
                stack.lore(lore);
                stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (GUIButton.isSameItem(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI().getInventory());
            p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
            return;
        }
        if (GUIButton.isSameItem(item, "weapon")) {
            PlayerCustomize.setWeaponSkin(p, item.getType());
            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
            update();
        }
    }

    private @NotNull ItemStack weapon(Material material) {
        ItemStack weapon = new ItemStack(material);
        weapon.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "weapon");
        });
        return weapon;
    }
}
