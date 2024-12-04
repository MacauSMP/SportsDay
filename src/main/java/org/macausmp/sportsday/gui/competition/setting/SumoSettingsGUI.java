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
    }

    @ButtonHandler("weapon")
    public void weapon(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        String path = event.getID() + ".enable_weapon";
        boolean weapon = PLUGIN.getConfig().getBoolean(path);
        PLUGIN.getConfig().set(path, !weapon);
        PLUGIN.saveConfig();
        updateGUI();
        p.playSound(Sound.sound(Key.key(weapon ? "minecraft:entity.enderman.teleport" : "minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack weapon() {
        boolean weapon = PLUGIN.getConfig().getBoolean(event.getID() + ".enable_weapon");
        return ItemUtil.item(weapon ? Material.BLAZE_ROD : Material.STICK, "weapon",
                Component.translatable("gui.event_settings.weapon").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(weapon ? "gui.enabled" : "gui.disabled")),
                "gui.toggle", "gui.event_settings.reload_require");
    }
}
