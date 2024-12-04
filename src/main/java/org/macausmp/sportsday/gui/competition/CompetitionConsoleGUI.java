package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.util.ItemUtil;

public class CompetitionConsoleGUI extends AbstractCompetitionGUI {
    private static final ItemStack START_COMPETITION = ItemUtil.head(ItemUtil.START, "start_competition", "gui.start.title", "gui.start.lore");
    private static final ItemStack LOAD_COMPETITION = ItemUtil.head(ItemUtil.START, "load_competition", "gui.load.title", "gui.load.lore");

    public CompetitionConsoleGUI() {
        super(27, Component.translatable("gui.console.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.setGlint(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, START_COMPETITION);
        getInventory().setItem(19, LOAD_COMPETITION);
    }

    @ButtonHandler("start_competition")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.openInventory(new CompetitionStartGUI().getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("load_competition")
    public void load(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        Competitions.loadEventData(p);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }
}
