package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.GraffitiSpray;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class GraffitiSprayGUI extends PluginGUI {
    private final PageBox<GraffitiSpray> pageBox = new PageBox<>(this, 10, 54,
            () -> List.of(GraffitiSpray.values()));
    private GraffitiSpray selected;

    public GraffitiSprayGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.graffiti_spray.title"));
        selected = PlayerCustomize.getGraffitiSpray(player);
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        pageBox.updatePage(this::graffiti);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("graffiti")
    public void graffiti(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setGraffitiSpray(p, selected = GraffitiSpray.values()[e.getSlot() - 10]);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setGraffitiSpray(p, selected = null);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack graffiti(@NotNull GraffitiSpray graffiti) {
        ItemStack stack = ItemUtil.item(Material.PAINTING, "graffiti", graffiti.getName(),
                graffiti == selected ? "gui.selected" : "gui.select");
        if (graffiti == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.graffiti_spray.reset");
    }
}
