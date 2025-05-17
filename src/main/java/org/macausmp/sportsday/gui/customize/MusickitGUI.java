package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.Musickit;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class MusickitGUI extends PluginGUI {
    private final PageBox<Musickit> pageBox = new PageBox<>(this, 1, 45,
            () -> List.of(Musickit.values()));
    private Musickit selected;

    public MusickitGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.musickit.title"));
        selected = PlayerCustomize.getMusickit(player);
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(0, reset());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::musickit);
    }

    private @NotNull ItemStack musickit(@NotNull Musickit musickit) {
        ItemStack stack = ItemUtil.item(Material.JUKEBOX, "musickit", musickit, "gui.customize.musickit.view",
                musickit == selected ? "gui.selected" : "gui.select");
        if (musickit == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.musickit.reset");
    }

    @ButtonHandler("musickit")
    public void musickit(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() - 1 + pageBox.getSize() * pageBox.getPage();
        Musickit musickit = selected = Musickit.values()[i];
        if (e.isRightClick()) {
            p.stopAllSounds();
            p.playSound(Sound.sound(musickit.key(), Sound.Source.MASTER, 1f, 1f));
        } else {
            PlayerCustomize.setMusickit(p, musickit);
            p.playSound(EXECUTION_SUCCESS_SOUND);
            update();
        }
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setMusickit(p, selected = null);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
