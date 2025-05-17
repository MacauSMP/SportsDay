package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class SportsSelectGUI extends PluginGUI {
    private final PluginGUI prev;
    private final Consumer<Sport> consumer;

    public SportsSelectGUI(@NotNull PluginGUI prev, @NotNull Consumer<Sport> consumer) {
        super(18, Component.translatable("gui.sport.select.title"));
        this.prev = prev;
        this.consumer = consumer;
        for (int i = 9; i < 18; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(17, BACK);
        Iterator<Sport> iterator = SportsRegistry.SPORT.iterator();
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (!iterator.hasNext())
                break;
            getInventory().setItem(i, sport(iterator.next()));
        }
    }

    private @NotNull ItemStack sport(@NotNull Sport sport) {
        ItemStack stack = ItemUtil.item(sport.getDisplayItem(), "sport", sport,
                sport.getSportType().asComponent().color(NamedTextColor.GRAY), "gui.sport.select.lore");
        stack.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE, sport.getKey());
        });
        return stack;
    }

    @ButtonHandler("sport")
    public void sport(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        Sport sport = SportsRegistry.SPORT.get(Objects.requireNonNull(item.getItemMeta()
                .getPersistentDataContainer().get(ItemUtil.EVENT_ID,KeyDataType.KEY_DATA_TYPE)));
        consumer.accept(sport);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        prev.open(p);
    }
}
