package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

public class MenuGUI extends PluginGUI {
    private static final ItemStack GUIDEBOOK = ItemUtil.item(Material.WRITABLE_BOOK, "guidebook", Component.translatable("gui.menu.guidebook.title").color(NamedTextColor.YELLOW), "gui.menu.guidebook.lore");
    private static final ItemStack HOME = ItemUtil.item(Material.RED_BED, "home", Component.translatable("gui.menu.home.title").color(NamedTextColor.YELLOW), "gui.menu.home.lore");
    private static final ItemStack SCHEDULE = ItemUtil.item(Material.PAINTING, "schedule", Component.translatable("gui.menu.schedule.title").color(NamedTextColor.YELLOW), "gui.menu.schedule.lore");
    private static final ItemStack TRAINING = ItemUtil.item(Material.ARMOR_STAND, "training", Component.translatable("gui.menu.training.title").color(NamedTextColor.YELLOW), "gui.menu.training.lore");
    private static final Book GUIDE_BOOK = guidebook();

    public MenuGUI() {
        super(27, Component.translatable("gui.menu.title"));
        getInventory().setItem(10, GUIDEBOOK);
        getInventory().setItem(12, HOME);
        getInventory().setItem(14, SCHEDULE);
        getInventory().setItem(16, TRAINING);
    }

    @ButtonHandler("guidebook")
    public void guide(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openBook(GUIDE_BOOK);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("home")
    public void home(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (SportsDay.getCurrentEvent() == null) {
            p.teleport(p.getWorld().getSpawnLocation());
            p.playSound(TELEPORT_SOUND);
        } else {
            p.sendMessage(Component.translatable("event.function_disable"));
            p.playSound(EXECUTION_FAIL_SOUND);
            p.closeInventory();
        }
    }

    @ButtonHandler("schedule")
    public void schedule(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ScheduleGUI().open(p);
    }

    @ButtonHandler("training")
    public void training(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (SportsDay.getCurrentEvent() == null) {
            new TrainingGUI().open(p);
        } else {
            p.sendMessage(Component.translatable("event.function_disable"));
            p.playSound(EXECUTION_FAIL_SOUND);
            p.closeInventory();
        }
    }

    private static @NotNull Book guidebook() {
        Book.Builder builder = Book.builder();
        builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_main")));
        builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_rule_track")));
        for (Sport sport : SportsRegistry.SPORT) {
            TranslatableComponent rule = Component.translatable("sport.desc." + sport.getKey().getKey());
            builder.addPage(TextUtil.text(Component.translatable("book.guidebook.page_event")
                    .arguments(sport.asComponent().color(NamedTextColor.BLACK), rule)));
        }
        return builder.build();
    }
}
