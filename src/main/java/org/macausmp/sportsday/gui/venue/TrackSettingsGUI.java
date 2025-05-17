package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Track;

public class TrackSettingsGUI extends VenueSettingsGUI<Track> {
    public TrackSettingsGUI(@NotNull Sport sport, @NotNull Track track) {
        super(54, sport, track);
        getInventory().setItem(32, startpoints());
        getInventory().setItem(34, checkpoints());
        update();
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(28, start());
        getInventory().setItem(30, end());
    }

    private @NotNull ItemStack start() {
        return ItemUtil.item(Material.RED_BED, "start",
                Component.translatable("gui.venue_settings.start_point.title"),
                Component.translatable("gui.venue_settings.start_point.lore"));
    }

    private @NotNull ItemStack end() {
        return ItemUtil.item(Material.GOLD_BLOCK, "end",
                Component.translatable("gui.venue_settings.end_point.title"),
                Component.translatable("gui.venue_settings.end_point.lore"));
    }

    private @NotNull ItemStack startpoints() {
        return ItemUtil.item(Material.ENDER_EYE, "startpoints",
                Component.translatable("gui.venue_settings.startpoints.title"),
                Component.translatable("gui.venue_settings.startpoints.lore"));
    }

    private @NotNull ItemStack checkpoints() {
        return ItemUtil.item(Material.BEACON, "checkpoints",
                Component.translatable("gui.venue_settings.checkpoints.title"),
                Component.translatable("gui.venue_settings.checkpoints.lore"));
    }

    @ButtonHandler("start")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new TrackPointSettingsGUI(this, venue.getStartPoint(), Component.translatable("gui.venue_settings.start_point.title")).open(p);
    }

    @ButtonHandler("end")
    public void end(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new TrackPointSettingsGUI(this, venue.getEndPoint(), Component.translatable("gui.venue_settings.end_point.title")).open(p);
    }

    @ButtonHandler("startpoints")
    public void startpoints(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new TrackPointListGUI(this, Component.translatable("gui.venue_settings.startpoint"), venue.getStartPoints(), Material.ENDER_EYE).open(p);
    }

    @ButtonHandler("checkpoints")
    public void checkpoints(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new TrackPointListGUI(this, Component.translatable("gui.venue_settings.checkpoint"), venue.getCheckPoints(), Material.BEACON).open(p);
    }
}
