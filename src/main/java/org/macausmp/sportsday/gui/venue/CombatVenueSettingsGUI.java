package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.CombatVenue;

public class CombatVenueSettingsGUI extends VenueSettingsGUI<CombatVenue> {
    public CombatVenueSettingsGUI(@NotNull Sport sport, @NotNull CombatVenue combatVenue) {
        super(54, sport, combatVenue);
        update();
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(30, p1());
        getInventory().setItem(32, p2());
    }

    private @NotNull ItemStack p1() {
        return ItemUtil.head(ItemUtil.ONE, "p1",
                Component.translatable("gui.venue_settings.p1_location.title"),
                Component.translatable("gui.venue_settings.p1_location.lore"));
    }

    private @NotNull ItemStack p2() {
        return ItemUtil.head(ItemUtil.TWO, "p2",
                Component.translatable("gui.venue_settings.p2_location.title"),
                Component.translatable("gui.venue_settings.p2_location.lore"));
    }

    @ButtonHandler("p1")
    public void p1(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        venue.setP1Location(p.getLocation());
        updateAll();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("p2")
    public void p2(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        venue.setP2Location(p.getLocation());
        updateAll();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
