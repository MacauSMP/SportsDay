package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.competition.event.EventGUI;
import org.macausmp.sportsday.gui.competition.event.JavelinGUI;
import org.macausmp.sportsday.gui.competition.event.SumoGUI;
import org.macausmp.sportsday.gui.competition.event.TrackEventGUI;
import org.macausmp.sportsday.util.ItemUtil;

public abstract class AbstractCompetitionGUI extends PluginGUI {
    protected static final ItemStack COMPETITION_CONSOLE = ItemUtil.item(Material.COMMAND_BLOCK, "competition_console", "gui.console.title", "gui.console.lore");
    protected static final ItemStack CONTESTANTS_LIST = ItemUtil.item(Material.PAPER, "contestants_list", "gui.contestants_list.title", "gui.contestants_list.lore");
    protected static final ItemStack COMPETITION_SETTINGS = ItemUtil.item(Material.REPEATER, "competition_settings", "gui.settings.title", "gui.settings.lore");
    @SuppressWarnings("deprecation")
    protected static final ItemStack VERSION = ItemUtil.item(Material.OAK_SIGN, "version", Component.translatable("gui.plugin_version").arguments(Component.text(SportsDay.getInstance().getDescription().getVersion())));

    public AbstractCompetitionGUI(int size, Component title) {
        super(size, title);
    }

    @ButtonHandler("competition_console")
    public void console(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        IEvent event = Competitions.getCurrentEvent();
        if (event == null || event.getStatus() == Status.ENDED) {
            p.openInventory(new CompetitionConsoleGUI().getInventory());
            return;
        }
        if (event == Competitions.JAVELIN_THROW)
            p.openInventory(new JavelinGUI((JavelinThrow) event).getInventory());
        else if (event == Competitions.SUMO)
            p.openInventory(new SumoGUI((Sumo) event).getInventory());
        else if (event instanceof ITrackEvent track)
            p.openInventory(new TrackEventGUI(track).getInventory());
        else
            p.openInventory(new EventGUI<>(event).getInventory());
    }

    @ButtonHandler("contestants_list")
    public void list(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new ContestantsListGUI().getInventory());
    }

    @ButtonHandler("competition_settings")
    public void settings(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitionSettingsGUI().getInventory());
    }
}
