package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.SportingEvent;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ItemUtil;

public class EventGUI<T extends SportingEvent> extends AbstractCompetitionGUI {
    private static final ItemStack PAUSE_COMPETITION = ItemUtil.head(ItemUtil.PAUSE, "pause_competition", "gui.pause.title", "gui.pause.lore");
    private static final ItemStack UNPAUSE_COMPETITION = ItemUtil.head(ItemUtil.START, "unpause_competition", "gui.unpause.title", "gui.unpause.lore");
    private static final ItemStack TERMINATE_COMPETITION = ItemUtil.item(Material.RED_CONCRETE, "terminate_competition", "gui.terminate.title", "gui.terminate.lore");
    protected final T event;

    public EventGUI(@NotNull T event) {
        super(54, Component.translatable("gui.event_console.title").arguments(event.getName().color(NamedTextColor.BLACK)));
        this.event = event;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.setGlint(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, status());
        getInventory().setItem(19, player());
        getInventory().setItem(23, PAUSE_COMPETITION);
        getInventory().setItem(24, UNPAUSE_COMPETITION);
        getInventory().setItem(26, TERMINATE_COMPETITION);
    }

    @Override
    public void update() {
        getInventory().setItem(18, status());
        getInventory().setItem(19, player());
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof EventGUI)
                .map(inv -> (EventGUI<? extends SportingEvent>) inv.getHolder())
                .forEach(EventGUI::update);
    }

    @ButtonHandler("pause_competition")
    public void pause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key(Competitions.pause(p) ?
                "minecraft:ui.button.click" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("unpause_competition")
    public void unpause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key(Competitions.unpause(p) ?
                "minecraft:ui.button.click" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("terminate_competition")
    public void terminate(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new ConfirmationGUI(this, player -> {
            boolean b = Competitions.terminate(player);
            player.playSound(Sound.sound(Key.key(b ?
                    "minecraft:ui.button.click" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return b;
        }).getInventory());
    }

    private @NotNull ItemStack status() {
        boolean b = Competitions.getCurrentEvent() != null;
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN)
                .arguments(b ? Competitions.getCurrentEvent().getName() : Component.translatable("gui.text.none"));
        Component lore = Component.translatable("competition.status").color(NamedTextColor.GREEN)
                .arguments(b ? Competitions.getCurrentEvent().getStatus() : Status.UPCOMING);
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.contestants.total").color(NamedTextColor.GREEN)
                .arguments(Component.text(Competitions.getContestants().size()).color(NamedTextColor.YELLOW));
        Component lore = Component.translatable("competition.contestants.online").color(NamedTextColor.GREEN)
                .arguments(Component.text(Competitions.getOnlineContestants().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display, lore);
    }
}
