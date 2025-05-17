package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PermissionRequired;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.TrackPoint;

@PermissionRequired
public class TrackPointSettingsGUI extends PluginGUI {
    private final PluginGUI prev;
    private final TrackPoint trackPoint;

    public TrackPointSettingsGUI(@NotNull PluginGUI prev, @NotNull TrackPoint trackPoint, @NotNull Component title) {
        super(36, title);
        this.prev = prev;
        this.trackPoint = trackPoint;
        for (int i = 27; i < 36; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(35, BACK);
        update();
    }

    @Override
    protected void update() {
        getInventory().setItem(12, location());
        getInventory().setItem(14, bb());
    }

    public static void updateTrackPoint(TrackPoint point) {
        updateAll(TrackPointSettingsGUI.class, gui -> gui.trackPoint == point);
    }

    private @NotNull ItemStack location() {
        Location loc = trackPoint.getLocation();
        return ItemUtil.item(Material.ENDER_PEARL, "location",
                Component.translatable("gui.venue_settings.track_point.location.title"),
                Component.translatable("gui.venue_settings.track_point.location.lore")
                        .arguments(Component.text(loc.x() + ", " + loc.y() + ", " + loc.z())));
    }

    private @NotNull ItemStack bb() {
        BoundingBox bb = trackPoint.getBoundingBox();
        ItemStack item = ItemUtil.item(Material.SPAWNER, "bb",
                Component.translatable("gui.venue_settings.track_point.bounding_box"),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore1"),
                Component.text(bb.getMinX() + ", " + bb.getMinY() + ", " + bb.getMinZ()).color(NamedTextColor.GRAY),
                Component.text(bb.getMaxX() + ", " + bb.getMaxY() + ", " + bb.getMaxZ()).color(NamedTextColor.GRAY),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore2"),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore3"));
        item.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        return item;
    }

    @ButtonHandler("location")
    public void location(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        trackPoint.setLocation(p.getLocation());
        updateTrackPoint(trackPoint);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("bb")
    public void bb(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!e.isLeftClick() && !e.isRightClick())
            return;
        Vector vec = p.getLocation().toVector();
        if (e.isLeftClick()) {
            trackPoint.setBoundingBoxCorner1(vec);
        } else {
            trackPoint.setBoundingBoxCorner2(vec);
        }
        updateTrackPoint(trackPoint);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        prev.open(p);
    }
}
