package org.macausmp.sportsday.gui.setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;

public class TrackSportSettingsGUI extends SportSettingsGUI {
    public TrackSportSettingsGUI(Sport sport) {
        super(sport);
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(2, laps());
        getInventory().setItem(3, checkpoints());
    }

    private @NotNull ItemStack laps() {
        int laps = sport.getSetting(Sport.TrackSettings.LAPS);
        ItemStack stack = ItemUtil.item(Material.LEAD, "laps",
                Component.translatable("gui.sport_settings.laps")
                        .arguments(Component.text(laps)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease");
        stack.setAmount(laps);
        return stack;
    }

    private @NotNull ItemStack checkpoints() {
        boolean checkpoints = sport.getSetting(Sport.TrackSettings.ALL_CHECKPOINTS_REQUIRED);
        return ItemUtil.item(Material.RED_BED, "checkpoints",
                Component.translatable("gui.sport_settings.all_checkpoints_required").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(checkpoints ? "gui.enabled" : "gui.disabled")),
                "gui.toggle");
    }

    @ButtonHandler("laps")
    public void laps(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setInteger(Sport.TrackSettings.LAPS, p, e.getClick());
    }

    @ButtonHandler("checkpoints")
    public void checkpoints(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setBoolean(Sport.TrackSettings.ALL_CHECKPOINTS_REQUIRED, p);
    }
}
