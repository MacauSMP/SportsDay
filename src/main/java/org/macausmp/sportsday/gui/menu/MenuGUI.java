package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.AbstractTrackEvent;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

public class MenuGUI extends PluginGUI {
    private static final ItemStack GUIDEBOOK = ItemUtil.item(Material.WRITABLE_BOOK, "guidebook", Component.translatable("gui.menu.guidebook.title").color(NamedTextColor.YELLOW), "gui.menu.guidebook.lore");
    private static final ItemStack HOME = ItemUtil.item(Material.RED_BED, "home", Component.translatable("gui.menu.home.title").color(NamedTextColor.YELLOW), "gui.menu.home.lore");
    private static final ItemStack PRACTICE = ItemUtil.item(Material.ARMOR_STAND, "practice", Component.translatable("gui.menu.practice.title").color(NamedTextColor.YELLOW), "gui.menu.practice.lore");
    private static final Book GUIDE_BOOK = guidebook();

    public MenuGUI() {
        super(9, Component.translatable("gui.menu.title"));
        getInventory().setItem(0, GUIDEBOOK);
        getInventory().setItem(1, HOME);
        getInventory().setItem(2, PRACTICE);
    }

    @ButtonHandler("guidebook")
    public void guide(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openBook(GUIDE_BOOK);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("home")
    public void home(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null) {
            p.teleport(p.getWorld().getSpawnLocation());
            p.playSound(Sound.sound(Key.key("minecraft:entity.bat.takeoff"), Sound.Source.MASTER, 1f, 1f));
        } else {
            p.sendMessage(Component.translatable("competition.function_disable"));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            p.closeInventory();
        }
    }

    @ButtonHandler("practice")
    public void practice(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null) {
            p.openInventory(new PracticeGUI().getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        } else {
            p.sendMessage(Component.translatable("competition.function_disable"));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            p.closeInventory();
        }
    }

    private static @NotNull Book guidebook() {
        Book.Builder builder = Book.builder();
        builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_main")));
        Component b1 = Component.translatable(AbstractTrackEvent.CHECKPOINT.translationKey()).color(NamedTextColor.GREEN);
        Component b2 = Component.translatable(AbstractTrackEvent.DEATH.translationKey()).color(NamedTextColor.RED);
        Component b3 = Component.translatable(AbstractTrackEvent.FINISH_LINE.translationKey()).color(NamedTextColor.GOLD);
        builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_block").arguments(b1, b2, b3)));
        for (IEvent event : Competitions.EVENTS.values()) {
            Component rule = Component.translatable("event.rule." + event.getID());
            builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_event").arguments(event.getName().color(NamedTextColor.BLACK), rule)));
        }
        return builder.build();
    }
}
