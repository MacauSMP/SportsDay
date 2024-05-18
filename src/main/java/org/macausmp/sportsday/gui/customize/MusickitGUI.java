package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.CustomizeMusickit;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class MusickitGUI extends PluginGUI {
    private static final NamespacedKey MUSICKIT = new NamespacedKey(PLUGIN, "musickit");
    private static final int START_INDEX = 10;
    private final Player player;

    public MusickitGUI(Player player) {
        super(54, Component.translatable("gui.customize.musickit.title"));
        this.player = player;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < CustomizeMusickit.values().length; i++)
            getInventory().setItem(i + START_INDEX, musickit(CustomizeMusickit.values()[i]));
        CustomizeMusickit musickit = PlayerCustomize.getMusickit(player.getPersistentDataContainer());
        if (musickit == null)
            return;
        for (int i = START_INDEX; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null)
                break;
            String key = stack.getItemMeta().getPersistentDataContainer().get(MUSICKIT, PersistentDataType.STRING);
            if (key != null && key.equals(musickit.name())) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("gui.customize.musickit.view")));
                lore.add(TextUtil.text(Component.translatable("gui.selected")));
                stack.lore(lore);
                stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new CustomizeMenuGUI(p).getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("musickit")
    public void musickit(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        CustomizeMusickit musickit = CustomizeMusickit.values()[e.getSlot() - START_INDEX];
        if (e.isRightClick()) {
            p.stopAllSounds();
            p.playSound(Sound.sound(musickit.getKey(), Sound.Source.MASTER, 1f, 1f));
        } else {
            PlayerCustomize.setMusickit(p, musickit);
            p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
            update();
        }
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setMusickit(p, null);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack musickit(@NotNull CustomizeMusickit musickit) {
        ItemStack stack = ItemUtil.item(Material.JUKEBOX, "musickit", musickit.getName(), "gui.customize.musickit.view", "gui.select");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(MUSICKIT, PersistentDataType.STRING, musickit.name()));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.musickit.reset");
    }
}
