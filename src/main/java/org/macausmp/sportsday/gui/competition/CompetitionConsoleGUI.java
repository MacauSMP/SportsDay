package org.macausmp.sportsday.gui.competition;

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
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class CompetitionConsoleGUI extends AbstractCompetitionGUI {
    private static final Set<CompetitionConsoleGUI> HANDLER = new HashSet<>();

    public CompetitionConsoleGUI() {
        super(27, Component.translatable("gui.console.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.addWrapper(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        if (hasEvent()) {
            getInventory().setItem(18, status());
            getInventory().setItem(19, player());
            getInventory().setItem(26, END_COMPETITION);
        } else {
            getInventory().setItem(18, START_COMPETITION);
            getInventory().setItem(19, LOAD_COMPETITION);
        }
        update();
    }

    @Override
    public void update() {
        if (hasEvent()) {
            getInventory().setItem(18, status());
            getInventory().setItem(19, player());
        }
        HANDLER.add(this);
    }

    public static void updateGUI() {
        HANDLER.forEach(CompetitionConsoleGUI::update);
    }

    @ButtonHandler("start_competitions")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitionStartGUI().getInventory());
    }

    @ButtonHandler("load_competitions")
    public void load(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        Competitions.loadEventData(p);
    }

    @ButtonHandler("end_competition")
    public void end(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.end.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new ConfirmationGUI(this, player -> {
            boolean b = Competitions.forceEnd(player);
            player.playSound(Sound.sound(Key.key(b ?
                    "minecraft:entity.enderman.teleport" : "minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return b;
        }).getInventory());
    }

    private @NotNull ItemStack status() {
        boolean b = Competitions.getCurrentEvent() != null;
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN)
                .arguments(b ? Competitions.getCurrentEvent().getName() : Component.translatable("gui.text.none"));
        Component lore = Component.translatable("competition.status").color(NamedTextColor.GREEN)
                .arguments(b ? Competitions.getCurrentEvent().getStatus().getName() : Status.IDLE.getName());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.contestants.total").color(NamedTextColor.GREEN)
                .arguments(Component.text(Competitions.getContestants().size()).color(NamedTextColor.YELLOW));
        Component lore = Component.translatable("competition.contestants.online").color(NamedTextColor.GREEN)
                .arguments(Component.text(Competitions.getOnlineContestants().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display, lore);
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }

    private static boolean hasEvent() {
        IEvent event = Competitions.getCurrentEvent();
        return event != null && event.getStatus() != Status.ENDED;
    }
}
