package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class BoatTypeGUI extends PluginGUI {
    private final Player player;

    public BoatTypeGUI(Player player) {
        super(18, Component.translatable("gui.customize.boat_type.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(9, boat(Material.OAK_BOAT));
        getInventory().setItem(10, boat(Material.SPRUCE_BOAT));
        getInventory().setItem(11, boat(Material.BIRCH_BOAT));
        getInventory().setItem(12, boat(Material.JUNGLE_BOAT));
        getInventory().setItem(13, boat(Material.ACACIA_BOAT));
        getInventory().setItem(14, boat(Material.DARK_OAK_BOAT));
        getInventory().setItem(15, boat(Material.MANGROVE_BOAT));
        getInventory().setItem(16, boat(Material.CHERRY_BOAT));
        Boat.Type type = PlayerCustomize.getBoatType(player);
        for (int i = 9; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            if (type == null || stack.getType().name().startsWith(type.name())) {
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

    @ButtonHandler("boat")
    public void boat(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setBoatType(p, item.getType());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack boat(Material material) {
        return ItemUtil.item(material, "boat", null, "gui.select");
    }
}
