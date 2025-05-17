package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Venue;

@PermissionRequired
public abstract class VenueSettingsGUI<V extends Venue> extends PluginGUI {
    private final Sport sport;
    protected final V venue;

    public VenueSettingsGUI(int size, @NotNull Sport sport, @NotNull V venue) {
        super(size, venue.asComponent());
        this.sport = sport;
        this.venue = venue;
        for (int i = size - 9; i < size; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(size - 1, BACK);
    }

    @Override
    protected void update() {
        getInventory().setItem(10, name());
        getInventory().setItem(12, item());
        getInventory().setItem(14, location());
        getInventory().setItem(16, delete());
    }

    protected @NotNull ItemStack name() {
        return ItemUtil.item(Material.NAME_TAG, "name",
                Component.translatable("gui.venue_settings.name.title"),
                Component.translatable("gui.venue_settings.name.lore"));
    }

    protected @NotNull ItemStack item() {
        return ItemUtil.item(venue.getItem(), "item",
                Component.translatable("gui.venue_settings.item.title"),
                Component.translatable("gui.venue_settings.item.lore"));
    }

    protected @NotNull ItemStack location() {
        Location loc = venue.getLocation();
        return ItemUtil.item(Material.ENDER_PEARL, "location",
                Component.translatable("gui.venue_settings.location.title"),
                Component.translatable("gui.venue_settings.location.lore1")
                        .arguments(Component.text(loc.x() + ", " + loc.y() + ", " + loc.z())),
                Component.translatable("gui.venue_settings.location.lore2"));
    }

    protected @NotNull ItemStack delete() {
        return ItemUtil.item(Material.CAULDRON, "delete",
                Component.translatable("gui.venue_settings.delete.title"),
                Component.translatable("gui.venue_settings.delete.lore"));
    }

    @ButtonHandler("name")
    public void name(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new AnvilGUI(() -> venue.getType().getSettingsGUI(sport, venue), p, venue.getName(), venue::setName);
        updateAll();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("item")
    public void item(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        if (itemStack.isEmpty()) {
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        itemStack.editMeta(meta -> {
           meta.displayName(null);
           meta.lore(null);
        });
        venue.setItem(itemStack);
        updateAll();
        p.playSound(EXECUTION_SUCCESS_SOUND);
    }

    @ButtonHandler("location")
    public void location(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        venue.setLocation(p.getLocation());
        updateAll();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("delete")
    public void delete(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ConfirmationGUI(this, player -> {
            sport.removeVenue(venue.getUUID());
            p.playSound(Sound.sound(Key.key("minecraft:item.bundle.drop_contents"), Sound.Source.MASTER, 1f, 1f));
            return new VenueListGUI<>(sport);
        }).open(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new VenueListGUI<>(sport).open(p);
    }
}
