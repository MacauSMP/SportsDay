package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.TrackPoint;

import java.util.List;

public class TrackPointListGUI extends PluginGUI {
    private final PluginGUI prev;
    private final Component type;
    private final List<TrackPoint> trackPoints;
    private final PageBox<TrackPoint> pageBox;
    private final Material material;

    public TrackPointListGUI(@NotNull PluginGUI prev, @NotNull Component type, @NotNull List<TrackPoint> trackPoints, @NotNull Material material) {
        super(54, Component.translatable("gui.venue_settings.trackpoints.title").arguments(type));
        this.prev = prev;
        this.type = type;
        this.trackPoints = trackPoints;
        this.pageBox = new PageBox<>(this, 0, 45, () -> trackPoints);
        this.material = material;
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(45, add());
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        getInventory().setItem(53, BACK);
        update();
    }

    @Override
    protected void update() {
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::trackpoint);
    }

    private @NotNull ItemStack trackpoint(@NotNull TrackPoint trackPoint) {
        BoundingBox bb = trackPoint.getBoundingBox();
        return ItemUtil.item(material, "trackpoint",
                Component.translatable("gui.venue_settings.trackpoint.title")
                        .arguments(type, Component.text(trackPoints.indexOf(trackPoint) + 1)),
                Component.translatable("gui.venue_settings.trackpoint.lore1"),
                Component.text(bb.getMinX() + ", " + bb.getMinY() + ", " + bb.getMinZ()).color(NamedTextColor.GRAY),
                Component.text(bb.getMaxX() + ", " + bb.getMaxY() + ", " + bb.getMaxZ()).color(NamedTextColor.GRAY),
                Component.translatable("gui.venue_settings.trackpoint.lore2").arguments(type),
                Component.translatable("gui.venue_settings.trackpoint.lore3").arguments(type));
    }

    private @NotNull ItemStack add() {
        return ItemUtil.head(ItemUtil.PLUS, "add", Component.translatable("gui.venue_settings.trackpoint.add.title").arguments(type),
                Component.translatable("gui.venue_settings.trackpoint.add.lore").arguments(type));
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.BEACON);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    @ButtonHandler("trackpoint")
    public void trackpoint(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() + pageBox.getSize() * pageBox.getPage();
        if (e.getClick().isRightClick()) {
            new ConfirmationGUI(this, player -> {
                trackPoints.remove(i);
                updateAll();
                p.playSound(UI_BUTTON_CLICK_SOUND);
                return this;
            }).open(p);
            return;
        }
        new TrackPointSettingsGUI(this, trackPoints.get(i),
                Component.translatable("gui.venue_settings.trackpoint.title").arguments(type, Component.text(i + 1))).open(p);
    }

    @ButtonHandler("add")
    public void add(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        trackPoints.add(new TrackPoint(p.getLocation(), new BoundingBox()));
        updateAll();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.nextPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.previousPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        prev.open(p);
    }
}
