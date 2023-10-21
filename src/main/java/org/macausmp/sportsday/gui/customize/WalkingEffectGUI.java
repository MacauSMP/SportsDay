package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.CustomizeParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class WalkingEffectGUI extends AbstractGUI {
    private static final int START_INDEX = 10;
    private final Player player;

    public WalkingEffectGUI(Player player) {
        super(54, Component.translatable("gui.customize.walking_effect.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < CustomizeParticleEffect.values().length; i++) {
            getInventory().setItem(i + START_INDEX, effect(CustomizeParticleEffect.values()[i]));
        }
        CustomizeParticleEffect effect = PlayerCustomize.getWalkingEffect(player);
        if (effect == null) return;
        for (int i = START_INDEX; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            if (effect.getMaterial().equals(stack.getType())) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("gui.selected")));
                stack.lore(lore);
                stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(item, "walking_effect")) {
            PlayerCustomize.setWalkingEffect(p, CustomizeParticleEffect.values()[e.getSlot() - START_INDEX]);
        } else if (ItemUtil.equals(item, reset())) {
            PlayerCustomize.setWalkingEffect(p, null);
        }
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    private @NotNull ItemStack effect(@NotNull CustomizeParticleEffect effect) {
        return ItemUtil.item(effect.getMaterial(), "walking_effect", effect.getName(), "gui.select");
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.walking_effect.reset");
    }
}
