package org.macausmp.sportsday.gui.competition;

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
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class CompetitionSettingsGUI extends AbstractCompetitionGUI {
    private static final Set<CompetitionSettingsGUI> HANDLER = new HashSet<>();

    public CompetitionSettingsGUI() {
        super(54, Component.translatable("gui.settings.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, ItemUtil.addWrapper(COMPETITION_SETTINGS));
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, ELYTRA_RACING);
        getInventory().setItem(19, ICE_BOAT_RACING);
        getInventory().setItem(20, JAVELIN_THROW);
        getInventory().setItem(21, OBSTACLE_COURSE);
        getInventory().setItem(22, PARKOUR);
        getInventory().setItem(23, SUMO);
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
        HANDLER.forEach(PluginGUI::update);
    }

    @ButtonHandler("status_toggle")
    public void toggle(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        IEvent event = Competitions.EVENTS.get(item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING));
        if (event == null)
            return;
        PLUGIN.getConfig().set(event.getID() + ".enable", !event.isEnable());
        PLUGIN.saveConfig();
        updateGUI();
        p.playSound(Sound.sound(Key.key(event.isEnable() ?
                "minecraft:entity.arrow.hit_player" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack status(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.item(
                event.isEnable() ? Material.LIME_DYE : Material.BARRIER,
                "status_toggle",
                event.isEnable() ? "gui.enabled" : "gui.disabled", "gui.toggle");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, PersistentDataType.STRING, event.getID()));
        return stack;
    }
}
