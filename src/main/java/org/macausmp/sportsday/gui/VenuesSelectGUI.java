package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.*;
import java.util.function.Consumer;

public class VenuesSelectGUI<T extends Venue> extends PluginGUI {
    private static final NamespacedKey VENUE_ID = new NamespacedKey(PLUGIN, "venue_id");
    private final PluginGUI prev;
    private final Sport sport;
    private final Map<UUID, T> map = new HashMap<>();
    private final PageBox<T> pageBox;
    private final Consumer<T> consumer;

    @SuppressWarnings("unchecked")
    public VenuesSelectGUI(@NotNull PluginGUI prev, @NotNull Sport sport, @NotNull Consumer<T> consumer) {
        super(54, Component.translatable("gui.venue.select.title"));
        this.prev = prev;
        this.sport = sport;
        this.sport.getVenues().forEach(v -> map.put(v.getUUID(), (T) v));
        this.pageBox = new PageBox<>(this, 0, 45, () -> new ArrayList<>(map.values()));
        this.consumer = consumer;
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        update();
    }

    @Override
    protected void update() {
        map.clear();
        //noinspection unchecked
        sport.getVenues().forEach(v -> map.put(v.getUUID(), (T) v));
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::venue);
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(sport.getDisplayItem());
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack venue(@NotNull T venue) {
        ItemStack stack = ItemUtil.item(venue.getItem(), "venue", Component.text(venue.getName()),
                "gui.venue.select.lore");
        stack.editMeta(meta -> meta.getPersistentDataContainer()
                .set(VENUE_ID, PersistentDataType.STRING, venue.getUUID().toString()));
        return stack;
    }

    @ButtonHandler("venue")
    public void venue(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        UUID uuid = UUID.fromString(Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer()
                .get(VENUE_ID, PersistentDataType.STRING)));
        consumer.accept(map.get(uuid));
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        prev.open(p);
    }
}
