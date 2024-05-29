package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.competition.event.JavelinGUI;
import org.macausmp.sportsday.gui.competition.event.SumoGUI;
import org.macausmp.sportsday.gui.competition.event.TrackEventGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCompetitionGUI extends PluginGUI {
    protected static final ItemStack COMPETITION_CONSOLE = ItemUtil.item(Material.COMMAND_BLOCK, "competition_console", "gui.console.title", "gui.console.lore");
    protected static final ItemStack CONTESTANTS_LIST = ItemUtil.item(Material.PAPER, "contestants_list", "gui.contestants_list.title", "gui.contestants_list.lore");
    protected static final ItemStack START_COMPETITION = ItemUtil.head(ItemUtil.START, "start_competitions", "gui.start.title", "gui.start.lore");
    protected static final ItemStack LOAD_COMPETITION = ItemUtil.head(ItemUtil.START, "load_competitions", "gui.load.title", "gui.load.lore");
    protected static final ItemStack END_COMPETITION = ItemUtil.item(Material.RED_CONCRETE, "end_competition", "gui.end.title", "gui.end.lore");
    protected static final ItemStack COMPETITION_SETTINGS = ItemUtil.item(Material.REPEATER, "competition_settings", "gui.settings.title", "gui.settings.lore");
    @SuppressWarnings("deprecation")
    protected static final ItemStack VERSION = ItemUtil.item(Material.OAK_SIGN, "version", Component.translatable("gui.plugin_version").arguments(Component.text(SportsDay.getInstance().getDescription().getVersion())));
    public static final ItemStack ELYTRA_RACING = event(Material.ELYTRA, Competitions.ELYTRA_RACING);
    public static final ItemStack ICE_BOAT_RACING = event(Material.OAK_BOAT, Competitions.ICE_BOAT_RACING);
    public static final ItemStack JAVELIN_THROW = event(Material.TRIDENT, Competitions.JAVELIN_THROW);
    public static final ItemStack OBSTACLE_COURSE = event(Material.OAK_FENCE_GATE, Competitions.OBSTACLE_COURSE);
    public static final ItemStack PARKOUR = event(Material.LEATHER_BOOTS, Competitions.PARKOUR);
    public static final ItemStack SUMO = event(Material.COD, Competitions.SUMO);

    private static @NotNull ItemStack event(Material material, IEvent event) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(event.getName());
            List<Component> lore = new ArrayList<>();
            if (event instanceof ITrackEvent e) {
                lore.add(TextUtil.text(Component.translatable("event.type.track").color(NamedTextColor.GRAY)));
                lore.add(TextUtil.text(Component.translatable("event.track.laps")
                        .arguments(Component.text((e.getMaxLaps()))).color(NamedTextColor.YELLOW)));
            } else if (event instanceof IFieldEvent) {
                lore.add(TextUtil.text(Component.translatable("event.type.field").color(NamedTextColor.GRAY)));
            }
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "event");
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID());
        });
        return stack;
    }

    public AbstractCompetitionGUI(int size, Component title) {
        super(size, title);
    }

    @ButtonHandler("competition_console")
    public void console(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        IEvent event = Competitions.getCurrentEvent();
        if (event != null && event.getStatus() != Status.ENDED) {
            if (event == Competitions.JAVELIN_THROW)
                p.openInventory(new JavelinGUI((JavelinThrow) event).getInventory());
            else if (event == Competitions.SUMO)
                p.openInventory(new SumoGUI((Sumo) event).getInventory());
            else if (event instanceof ITrackEvent track)
                p.openInventory(new TrackEventGUI(track).getInventory());
            else
                p.openInventory(new CompetitionConsoleGUI().getInventory());
        } else {
            p.openInventory(new CompetitionConsoleGUI().getInventory());
        }
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
