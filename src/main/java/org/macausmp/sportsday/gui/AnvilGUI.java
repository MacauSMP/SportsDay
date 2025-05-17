package org.macausmp.sportsday.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class AnvilGUI implements Listener {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private final Supplier<PluginGUI> prev;
    private final Player player;
    private final AnvilView view;
    private final Consumer<String> consumer;

    public AnvilGUI(@Nullable Supplier<PluginGUI> prev, @NotNull Player player, @NotNull String original, @NotNull Consumer<String> consumer) {
        this.prev = prev;
        this.player = player;
        this.view = Objects.requireNonNull((AnvilView) player.openAnvil(null, true));
        this.view.setItem(0, ItemUtil.item(Material.NAME_TAG, null, original));
        this.consumer = consumer;
        PLUGIN.getServer().getPluginManager().registerEvents(this, PLUGIN);
    }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getView() != view)
            return;
        e.setCancelled(true);
        if (view.getTopInventory().getResult() != null) {
            consumer.accept(view.getRenameText());
            view.setItem(0, null);
            if (prev != null)
                prev.get().open(player);
            else
                player.closeInventory();
        }
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (e.getView() != view)
            return;
        view.setItem(0, null);
        HandlerList.unregisterAll(this);
    }
}
