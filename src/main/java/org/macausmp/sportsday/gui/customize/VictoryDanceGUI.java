package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.customize.VictoryDance;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class VictoryDanceGUI extends PluginGUI {
    private final PageBox<VictoryDance> pageBox = new PageBox<>(this, 1, 45,
            () -> List.of(VictoryDance.values()));
    private VictoryDance selected;

    public VictoryDanceGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.victory_dance.title"));
        selected = PlayerCustomize.getVictoryDance(player);
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(0, reset());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::dance);
    }

    private @NotNull ItemStack dance(@NotNull VictoryDance victoryDance) {
        ItemStack stack = ItemUtil.item(victoryDance.getMaterial(), "victory_dance", victoryDance,
                victoryDance == selected ? "gui.selected" : "gui.select");
        if (victoryDance == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.victory_dance.reset");
    }

    @ButtonHandler("victory_dance")
    public void victoryDance(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() - 1 + pageBox.getSize() * pageBox.getPage();
        PlayerCustomize.setVictoryDance(p, selected = VictoryDance.values()[i]);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setVictoryDance(p, selected = null);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
