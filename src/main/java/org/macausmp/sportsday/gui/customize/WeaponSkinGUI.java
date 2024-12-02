package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class WeaponSkinGUI extends PluginGUI {
    private final PageBox<Material> pageBox = new PageBox<>(this, 9, 18,
            () -> List.of(Material.BLAZE_ROD, Material.BONE, Material.SHEARS, Material.BAMBOO,
                    Material.DEAD_BUSH, Material.SUGAR_CANE, Material.COD));
    private Material selected;

    public WeaponSkinGUI(@NotNull Player player) {
        super(18, Component.translatable("gui.customize.weapon_skin.title"));
        selected = PlayerCustomize.getWeaponSkin(player);
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        update();
    }

    @Override
    public void update() {
        pageBox.updatePage(this::weapon);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("weapon")
    public void weapon(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setWeaponSkin(p, selected = item.getType());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack weapon(Material material) {
        ItemStack stack = ItemUtil.item(material, "weapon", null, material == selected ? "gui.selected" : "gui.select");
        if (material == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }
}
