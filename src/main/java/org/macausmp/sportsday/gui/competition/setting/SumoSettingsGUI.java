package org.macausmp.sportsday.gui.competition.setting;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Sumo;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.util.ItemUtil;

public class SumoSettingsGUI extends EventSettingsGUI<Sumo>{
    public SumoSettingsGUI(Sumo event) {
        super(event);
    }

    @Override
    public void update() {
        super.update();
        getInventory().setItem(21, weapon());
        getInventory().setItem(22, weaponTime());
    }

    @ButtonHandler("weapon")
    public void weapon(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        boolean weapon = event.getSetting(Sumo.ENABLE_WEAPON);
        event.setSetting(Sumo.ENABLE_WEAPON, !weapon);
        p.playSound(Sound.sound(Key.key(weapon ?
                "minecraft:entity.enderman.teleport" : "minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("weapon_time")
    public void weaponTime(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!e.getClick().isLeftClick() && !e.getClick().isRightClick())
            return;
        event.setSetting(Sumo.WEAPON_TIME, Math.max(event.getSetting(Sumo.WEAPON_TIME) + (e.getClick().isLeftClick() ? 5 : -5), 0));
        p.playSound(Sound.sound(Key.key(e.getClick().isLeftClick() ?
                "minecraft:entity.arrow.hit_player" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack weapon() {
        boolean weapon = event.getSetting(Sumo.ENABLE_WEAPON);
        return ItemUtil.item(weapon ? Material.BLAZE_ROD : Material.STICK, "weapon",
                Component.translatable("gui.event_settings.weapon").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(weapon ? "gui.enabled" : "gui.disabled")),
                "gui.toggle");
    }

    private @NotNull ItemStack weaponTime() {
        int time = event.getSetting(Sumo.WEAPON_TIME);
        ItemStack stack = ItemUtil.item(Material.CLOCK, "weapon_time",
                Component.translatable("gui.event_settings.weapon_time").arguments(Component.text(time)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease");
        stack.setAmount(Math.max(time, 1));
        return stack;
    }
}
