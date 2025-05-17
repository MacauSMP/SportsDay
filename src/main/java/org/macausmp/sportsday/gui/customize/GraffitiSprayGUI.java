package org.macausmp.sportsday.gui.customize;

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
    private final PageBox<GraffitiSpray> pageBox = new PageBox<>(this, 1, 45,
            () -> List.of(GraffitiSpray.values()));
    private GraffitiSpray selected;

    public GraffitiSprayGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.graffiti_spray.title"));
        selected = PlayerCustomize.getGraffitiSpray(player);
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(0, reset());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::graffiti);
    }

    private @NotNull ItemStack graffiti(@NotNull GraffitiSpray graffiti) {
        ItemStack stack = ItemUtil.item(Material.PAINTING, "graffiti", graffiti,
                graffiti == selected ? "gui.selected" : "gui.select");
        if (graffiti == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.graffiti_spray.reset");
    }

    @ButtonHandler("graffiti")
    public void graffiti(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() - 1 + pageBox.getSize() * pageBox.getPage();
        PlayerCustomize.setGraffitiSpray(p, selected = GraffitiSpray.values()[i]);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setGraffitiSpray(p, selected = null);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
