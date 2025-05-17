package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.VenuesSelectGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;

import java.util.Iterator;
import java.util.Objects;

public class TrainingGUI extends PluginGUI {
    public TrainingGUI() {
        super(18, Component.translatable("gui.menu.training.title"));
        for (int i = 9; i < 18; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(17, BACK);
        Iterator<Sport> iterator = SportsRegistry.SPORT.iterator();
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (!iterator.hasNext())
                break;
            getInventory().setItem(i, training(iterator.next()));
        }
    }

    private @NotNull ItemStack training(@NotNull Sport sport) {
        ItemStack stack = ItemUtil.item(sport.getDisplayItem(), "sport_training",
                Component.translatable("gui.menu.training.venue").arguments(sport),
                sport.getSportType().asComponent().color(NamedTextColor.GRAY));
        stack.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE, sport.getKey());
        });
        return stack;
    }

    @ButtonHandler("sport_training")
    public void training(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (SportsDay.getCurrentEvent() == null) {
            Sport sport = SportsRegistry.SPORT.get(Objects.requireNonNull(item.getItemMeta()
                    .getPersistentDataContainer().get(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE)));
            if (sport == null)
                return;
            new VenuesSelectGUI<>(this, sport, venue -> sport.getTrainingHandler().joinTraining(p, venue)).open(p);
        }
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new MenuGUI().open(p);
    }
}
