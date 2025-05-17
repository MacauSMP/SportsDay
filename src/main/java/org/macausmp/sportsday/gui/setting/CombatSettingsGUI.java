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

public class CombatSettingsGUI extends SportSettingsGUI {
    public CombatSettingsGUI(Sport sport) {
        super(sport);
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(2, weapon());
        getInventory().setItem(3, weaponTime());
    }

    private @NotNull ItemStack weapon() {
        boolean weapon = sport.getSetting(Sport.CombatSettings.ENABLE_WEAPON);
        return ItemUtil.item(weapon ? Material.BLAZE_ROD : Material.STICK, "weapon",
                Component.translatable("gui.sport_settings.weapon").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(weapon ? "gui.enabled" : "gui.disabled")),
                "gui.toggle");
    }

    private @NotNull ItemStack weaponTime() {
        int time = sport.getSetting(Sport.CombatSettings.WEAPON_TIME);
        ItemStack stack = ItemUtil.item(Material.CLOCK, "weapon_time",
                Component.translatable("gui.sport_settings.weapon_time").arguments(Component.text(time)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease");
        stack.setAmount(Math.max(time, 1));
        return stack;
    }

    @ButtonHandler("weapon")
    public void weapon(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setBoolean(Sport.CombatSettings.ENABLE_WEAPON, p);
    }

    @ButtonHandler("weapon_time")
    public void weaponTime(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setInteger(Sport.CombatSettings.WEAPON_TIME, p, e.getClick());
    }
}
