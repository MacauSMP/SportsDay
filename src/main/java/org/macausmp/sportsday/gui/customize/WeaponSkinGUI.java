package org.macausmp.sportsday.gui.customize;

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
    private final PageBox<Material> pageBox = new PageBox<>(this, 0, 18,
            () -> List.of(Material.BLAZE_ROD, Material.BONE, Material.SHEARS, Material.BAMBOO,
                    Material.DEAD_BUSH, Material.SUGAR_CANE, Material.COD));
    private Material selected;

    public WeaponSkinGUI(@NotNull Player player) {
        super(27, Component.translatable("gui.customize.weapon_skin.title"));
        selected = PlayerCustomize.getWeaponSkin(player);
        for (int i = 18; i < 27; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(26, BACK);
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::skin);
    }

    private @NotNull ItemStack skin(Material material) {
        ItemStack stack = ItemUtil.item(material, "skin", null, material == selected ? "gui.selected" : "gui.select");
        if (material == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    @ButtonHandler("skin")
    public void skin(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setWeaponSkin(p, selected = item.getType());
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
