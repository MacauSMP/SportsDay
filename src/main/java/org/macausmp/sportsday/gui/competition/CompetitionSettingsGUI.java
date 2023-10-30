package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionSettingsGUI extends AbstractCompetitionGUI {
    private static final List<CompetitionSettingsGUI> HANDLER = new ArrayList<>();

    public CompetitionSettingsGUI() {
        super(54, Component.translatable("gui.settings.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, ItemUtil.addEffect(GUIButton.COMPETITION_SETTINGS));
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
        update();
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(27, status(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, status(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, status(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, status(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, status(Competitions.PARKOUR));
        getInventory().setItem(32, status(Competitions.SUMO));
    }

    public static void updateGUI() {
        for (CompetitionSettingsGUI gui : HANDLER) {
            gui.update();
        }
    }

    @Override
    public void onClick(@NotNull Player p, @NotNull ItemStack item) {
        String id = item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING);
        if (ItemUtil.equals(item, "status_toggle")) {
            for (IEvent event : Competitions.COMPETITIONS) {
                if (event.getID().equals(id)) {
                    PLUGIN.getConfig().set(event.getID() + ".enable", !event.isEnable());
                    PLUGIN.saveConfig();
                    updateGUI();
                    p.playSound(Sound.sound(Key.key(event.isEnable() ? "minecraft:entity.arrow.hit_player" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
                    return;
                }
            }
        }
    }

    private @NotNull ItemStack status(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.item(event.isEnable() ? Material.LIME_DYE : Material.BARRIER, "status_toggle", event.isEnable() ? "gui.enabled" : "gui.disabled", "gui.toggle");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID()));
        return stack;
    }
}
