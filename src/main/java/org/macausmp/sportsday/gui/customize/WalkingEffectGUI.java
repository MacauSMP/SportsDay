package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.ParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;

public class WalkingEffectGUI extends PluginGUI {
    private final PageBox<ParticleEffect> pageBox = new PageBox<>(this, 1, 45,
            () -> List.of(ParticleEffect.values()));
    private ParticleEffect selected;

    public WalkingEffectGUI(@NotNull Player player) {
        super(54, Component.translatable("gui.customize.walking_effect.title"));
        selected = PlayerCustomize.getWalkingEffect(player);
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(0, reset());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::effect);
    }

    private @NotNull ItemStack effect(@NotNull ParticleEffect effect) {
        ItemStack stack = ItemUtil.item(effect.getMaterial(), "walking_effect", effect,
                effect == selected ? "gui.selected" : "gui.select");
        if (effect == selected)
            stack.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return stack;
    }

    private @NotNull ItemStack reset() {
        return ItemUtil.item(Material.BARRIER, "reset", "gui.customize.walking_effect.reset");
    }

    @ButtonHandler("walking_effect")
    public void walkingEffect(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() - 1 + pageBox.getSize() * pageBox.getPage();
        PlayerCustomize.setWalkingEffect(p, selected = ParticleEffect.values()[i]);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("reset")
    public void reset(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PlayerCustomize.setWalkingEffect(p, selected = null);
        p.playSound(EXECUTION_SUCCESS_SOUND);
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new CustomizeMenuGUI(p).open(p);
    }
}
