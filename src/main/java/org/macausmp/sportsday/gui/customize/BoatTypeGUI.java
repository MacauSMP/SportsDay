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

public class BoatTypeGUI extends PluginGUI {
    private final PageBox<Material> pageBox = new PageBox<>(this, 9, 18,
            () -> List.of(Material.OAK_BOAT, Material.SPRUCE_BOAT, Material.BIRCH_BOAT, Material.JUNGLE_BOAT,
                    Material.ACACIA_BOAT, Material.DARK_OAK_BOAT, Material.MANGROVE_BOAT, Material.CHERRY_BOAT,
                    Material.BAMBOO_RAFT));
    private Material selected;

    public BoatTypeGUI(@NotNull Player player) {
        super(18, Component.translatable("gui.customize.boat_type.title"));
        selected = Material.getMaterial(PlayerCustomize.getBoatType(player).name());
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        update();
    }

    @Override
    public void update() {
        pageBox.updatePage(this::boat);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("boat")
    public void boat(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setBoatType(p, selected = item.getType());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack boat(Material material) {
        ItemStack stack = ItemUtil.item(material, "boat", null, material == selected ? "gui.selected" : "gui.select");
        if (material == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }
}
