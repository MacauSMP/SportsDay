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
import org.macausmp.sportsday.customize.CustomizeGraffitiSpray;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class GraffitiSprayGUI extends PluginGUI {
    private static final NamespacedKey GRAFFITI_SPRAY = new NamespacedKey(PLUGIN, "graffiti_spray");
    private static final int START_INDEX = 10;
    private final Player player;

    public GraffitiSprayGUI(Player player) {
        super(54, Component.translatable("gui.customize.graffiti_spray.title"));
        this.player = player;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < CustomizeGraffitiSpray.values().length; i++)
            getInventory().setItem(i + START_INDEX, graffiti(CustomizeGraffitiSpray.values()[i]));
        CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(player);
        if (graffiti == null)
            return;
        for (int i = START_INDEX; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null)
                break;
            String key = stack.getItemMeta().getPersistentDataContainer().get(GRAFFITI_SPRAY, PersistentDataType.STRING);
            if (key != null && key.equals(graffiti.name())) {
                List<Component> lore = new ArrayList<>();
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

    @ButtonHandler("graffiti")
    public void graffiti(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setGraffitiSpray(p, CustomizeGraffitiSpray.values()[e.getSlot() - START_INDEX]);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setGraffitiSpray(p, null);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack graffiti(@NotNull CustomizeGraffitiSpray graffiti) {
        ItemStack stack = ItemUtil.item(Material.PAINTING, "graffiti", graffiti.getName(), "gui.select");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(GRAFFITI_SPRAY, PersistentDataType.STRING, graffiti.name()));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.graffiti_spray.reset");
    }
}
