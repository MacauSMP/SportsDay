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
import org.macausmp.sportsday.customize.VictoryDance;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class VictoryDanceGUI extends PluginGUI {
    private final PageBox<VictoryDance> pageBox = new PageBox<>(this, 10, 54,
            () -> List.of(VictoryDance.values()));
    private VictoryDance selected;

    public VictoryDanceGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.victory_dance.title"));
        selected = PlayerCustomize.getVictoryDance(player);
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        pageBox.updatePage(this::victoryDance);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("victory_dance")
    public void victoryDance(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setVictoryDance(p, selected = VictoryDance.values()[e.getSlot() - 10]);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setVictoryDance(p, selected = null);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack victoryDance(@NotNull VictoryDance victoryDance) {
        ItemStack stack = ItemUtil.item(victoryDance.getMaterial(), "victory_dance", victoryDance.getName(),
                victoryDance == selected ? "gui.selected" : "gui.select");
        if (victoryDance == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.victory_dance.reset");
    }
}
