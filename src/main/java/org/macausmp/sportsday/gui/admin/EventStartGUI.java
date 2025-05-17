package org.macausmp.sportsday.gui.admin;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.util.ItemUtil;

@PermissionRequired
public class EventStartGUI extends PluginGUI {
    private static final ItemStack START_EVENT = ItemUtil.head(ItemUtil.START, "start_event", "gui.start_event.title", "gui.start_event.lore");
    private static final ItemStack LOAD_EVENT = ItemUtil.item(Material.JUKEBOX, "load_event", "gui.load.title", "gui.load.lore");
    private static final ItemStack SCHEDULE = ItemUtil.item(Material.PAINTING, "schedule", "gui.menu.schedule.title", "gui.menu.schedule.lore");

    public EventStartGUI() {
        super(36, Component.translatable("gui.start_event.title"));
        for (int i = 27; i < 36; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(35, BACK);
        getInventory().setItem(11, START_EVENT);
        getInventory().setItem(13, LOAD_EVENT);
        getInventory().setItem(15, SCHEDULE);
    }

    @ButtonHandler("start_event")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new SportsSelectGUI(this, sport -> p.openInventory(new VenuesSelectGUI<>(this, sport,
                v -> {
                    if (!SportsDay.startEvent(p, sport, v))
                        p.playSound(EXECUTION_FAIL_SOUND);
                }).getInventory())
        ).open(p);
    }

    @ButtonHandler("load_event")
    public void load(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new EventLoadGUI().open(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new AdminMenuGUI().open(p);
    }
}
