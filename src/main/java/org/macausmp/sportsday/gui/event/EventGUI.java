package org.macausmp.sportsday.gui.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.EventStatus;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

public class EventGUI<T extends SportingEvent> extends PluginGUI {
    private static final ItemStack PAUSE_EVENT = ItemUtil.head(ItemUtil.PAUSE, "pause", "gui.event.pause.title", "gui.event.pause.lore");
    private static final ItemStack UNPAUSE_EVENT = ItemUtil.head(ItemUtil.START, "unpause", "gui.event.unpause.title", "gui.event.unpause.lore");
    private static final ItemStack SAVE_EVENT = ItemUtil.item(Material.CHEST, "save", "gui.event.save.title", "gui.event.save.lore");
    private static final ItemStack TERMINATE_EVENT = ItemUtil.item(Material.RED_CONCRETE, "terminate", "gui.event.terminate.title", "gui.event.terminate.lore");
    protected final T event;

    public EventGUI(@NotNull T event) {
        super(54, Component.translatable("gui.event.title").arguments(event.asComponent().color(NamedTextColor.BLACK)));
        this.event = event;
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(0, status());
        getInventory().setItem(1, player());
        getInventory().setItem(6, event.isPaused() ? UNPAUSE_EVENT : PAUSE_EVENT);
        getInventory().setItem(7, SAVE_EVENT);
        getInventory().setItem(8, TERMINATE_EVENT);
        getInventory().setItem(53, BACK);
    }

    @Override
    protected void update() {
        getInventory().setItem(0, status());
        getInventory().setItem(1, player());
        getInventory().setItem(6, event.isPaused() ? UNPAUSE_EVENT : PAUSE_EVENT);
    }

    private @NotNull ItemStack status() {
        Component display = Component.translatable("event.current").color(NamedTextColor.GREEN).arguments(event);
        Component lore = Component.translatable("event.status").color(NamedTextColor.GREEN).arguments(event.getStatus());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("event.contestants.total").color(NamedTextColor.GREEN)
                .arguments(Component.text(event.getContestants().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display);
    }

    @ButtonHandler("pause")
    public void pause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        p.playSound(event.pause(p) ? UI_BUTTON_CLICK_SOUND : EXECUTION_FAIL_SOUND);
        updateAll();
    }

    @ButtonHandler("unpause")
    public void unpause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        p.playSound(event.unpause(p) ? UI_BUTTON_CLICK_SOUND : EXECUTION_FAIL_SOUND);
        updateAll();
    }

    @ButtonHandler("save")
    public void save(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        SportsDay.saveEvent(p);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("terminate")
    public void terminate(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        new ConfirmationGUI(this, player -> {
            event.terminate(player);
            player.playSound(UI_BUTTON_CLICK_SOUND);
            return null;
        }).open(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new AdminMenuGUI().open(p);
    }
}
