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
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerCustomize;

import java.util.Objects;

public class MenuGUI extends AbstractGUI {
    private static final ItemStack GUIDEBOOK = ItemUtil.item(Material.WRITABLE_BOOK, "guidebook", Component.translatable("gui.menu.guidebook.title").color(NamedTextColor.YELLOW), "gui.menu.guidebook.lore");
    private static final ItemStack HOME = ItemUtil.item(Material.RED_BED, "home", Component.translatable("gui.menu.home.title").color(NamedTextColor.YELLOW), "gui.menu.home.lore");
    private static final ItemStack PRACTICE = ItemUtil.item(Material.ARMOR_STAND, "practice", Component.translatable("gui.menu.practice.title").color(NamedTextColor.YELLOW), "gui.menu.practice.lore");
    private static final Book GUIDE_BOOK =  guidebook();

    public MenuGUI() {
        super(9, Component.translatable("gui.menu.title"));
        getInventory().setItem(0, GUIDEBOOK);
        getInventory().setItem(1, HOME);
        getInventory().setItem(2, PRACTICE);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        if (ItemUtil.equals(item, GUIDEBOOK)) {
            p.openBook(GUIDE_BOOK);
        } else if (ItemUtil.equals(item, HOME)) {
            if (Competitions.getCurrentEvent() == null) {
                p.teleport(p.getWorld().getSpawnLocation());
                p.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:entity.bat.takeoff"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f), net.kyori.adventure.sound.Sound.Emitter.self());
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(3, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            }
        } else if (ItemUtil.equals(item, PRACTICE)) {
            if (Competitions.getCurrentEvent() == null) {
                p.openInventory(new PracticeGUI().getInventory());
            }
        }
    }

    private static @NotNull Book guidebook() {
        Book.Builder builder = Book.builder();
        builder.addPage(Component.translatable("book.guidebook.page_main"));
        Component b1 = Component.translatable(Objects.requireNonNull(CompetitionListener.CHECKPOINT).translationKey()).color(NamedTextColor.GREEN);
        Component b2 = Component.translatable(Objects.requireNonNull(CompetitionListener.DEATH).translationKey()).color(NamedTextColor.RED);
        Component b3 = Component.translatable(Objects.requireNonNull(AbstractTrackEvent.FINISH_LINE).translationKey()).color(NamedTextColor.GOLD);
        builder.addPage(Component.translatable("book.guidebook.page_block").args(b1, b2, b3));
        for (IEvent event : Competitions.COMPETITIONS) {
            Component rule = Component.translatable("event.rule." + event.getID());
            builder.addPage(Component.translatable("book.guidebook.page_event").args(event.getName().color(NamedTextColor.BLACK), rule));
        }
        return builder.build();
    }
}
