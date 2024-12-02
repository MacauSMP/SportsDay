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
import org.macausmp.sportsday.competition.ITrackEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.util.ItemUtil;

public class TrackEventSettingsGUI extends EventSettingsGUI<ITrackEvent> {
    public TrackEventSettingsGUI(ITrackEvent event) {
        super(event);
    }

    @Override
    public void update() {
        super.update();
        getInventory().setItem(21, laps());
    }

    @ButtonHandler("laps")
    public void laps(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!e.getClick().isLeftClick() && !e.getClick().isRightClick())
            return;
        String path = event.getID() + ".laps";
        int laps = PLUGIN.getConfig().getInt(path) + (e.getClick().isLeftClick() ? 1 : -1);
        if (laps > 0) {
            PLUGIN.getConfig().set(path, laps);
            PLUGIN.saveConfig();
            updateGUI();
        }
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack laps() {
        int laps = PLUGIN.getConfig().getInt(event.getID() + ".laps");
        ItemStack stack = ItemUtil.item(Material.LEAD, "laps",
                Component.translatable("gui.event_settings.laps").arguments(Component.text(laps)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease", "gui.event_settings.reload_require");
        stack.setAmount(laps);
        return stack;
    }
}
