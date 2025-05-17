package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.*;

@PermissionRequired
public class VenueListGUI<V extends Venue> extends PluginGUI {
    private static final NamespacedKey VENUE_ID = new NamespacedKey(PLUGIN, "venue_id");
    private final Sport sport;
    private final Map<UUID, V> map = new HashMap<>();
    private final PageBox<V> pageBox;

    @SuppressWarnings("unchecked")
    public VenueListGUI(@NotNull Sport sport) {
        super(54, Component.translatable("gui.venue.title").arguments(sport.asComponent().color(NamedTextColor.BLACK)));
        this.sport = sport;
        this.sport.getVenues().forEach(v -> map.put(v.getUUID(), (V) v));
        this.pageBox = new PageBox<>(this, 0, 45, () -> new ArrayList<>(map.values()));
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        getInventory().setItem(45, add());
        update();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void update() {
        map.clear();
        sport.getVenues().forEach(v -> map.put(v.getUUID(), (V) v));
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::venue);
    }

    private @NotNull ItemStack pages() {
        return ItemUtil.item(sport.getDisplayItem(), null,
                Component.translatable("book.pageIndicator")
                        .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage())));
    }

    private @NotNull ItemStack venue(@NotNull V venue) {
        ItemStack stack = ItemUtil.item(venue.getItem(), "venue", Component.text(venue.getName()),
                Component.translatable("gui.venue.select.lore"));
        stack.editMeta(meta -> meta.getPersistentDataContainer()
                .set(VENUE_ID, PersistentDataType.STRING, venue.getUUID().toString()));
        return stack;
    }

    private @NotNull ItemStack add() {
        return ItemUtil.head(ItemUtil.PLUS, "add", Component.translatable("gui.venue.add.title"),
                Component.translatable("gui.venue.add.lore"));
    }

    @ButtonHandler("venue")
    public void venue(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        V venue = map.get(UUID.fromString(Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer()
                .get(VENUE_ID, PersistentDataType.STRING))));
        venue.getType().getSettingsGUI(sport, venue).open(p);
    }

    @SuppressWarnings("unchecked")
    @ButtonHandler("add")
    public void add(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        V venue = (V) sport.addVenue(null, p.getLocation());
        updateAll();
        venue.getType().getSettingsGUI(sport, venue).open(p);
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
        new SportsSelectGUI(new AdminMenuGUI(), sport -> p.openInventory(new VenueListGUI<>(sport).getInventory())).open(p);
    }
}
