package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

public class PracticeGUI extends AbstractGUI {
    public PracticeGUI() {
        super(27, Component.translatable("gui.menu.practice.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(9, GUIButton.ELYTRA_RACING);
        getInventory().setItem(10, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(11, GUIButton.JAVELIN_THROW);
        getInventory().setItem(12, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(13, GUIButton.PARKOUR);
        getInventory().setItem(14, GUIButton.SUMO);
    }

    @Override
    public void update() {
        getInventory().setItem(18, practice(Competitions.ELYTRA_RACING));
        getInventory().setItem(19, practice(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(20, practice(Competitions.JAVELIN_THROW));
        getInventory().setItem(21, practice(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(22, practice(Competitions.PARKOUR));
        getInventory().setItem(23, practice(Competitions.SUMO));
    }

    @Override
    public void onClick(InventoryClickEvent e, Player p, ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.BACK)) {
            p.openInventory(new MenuGUI().getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStage() != Stage.STARTED) {
            String id = item.getItemMeta().getPersistentDataContainer().get(ItemUtil.COMPETITION_ID, PersistentDataType.STRING);
            if (ItemUtil.equals(item, "practice")) {
                for (IEvent event : Competitions.COMPETITIONS) {
                    if (event.getID().equals(id)) {
                        event.practice(p);
                        return;
                    }
                }
            }
        }
    }

    private @NotNull ItemStack practice(@NotNull IEvent event) {
        ItemStack stack = new ItemStack(Material.OAK_DOOR);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.menu.practice").args(event.getName())));
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "practice");
            meta.getPersistentDataContainer().set(ItemUtil.COMPETITION_ID, PersistentDataType.STRING, event.getID());
        });
        return stack;
    }
}
