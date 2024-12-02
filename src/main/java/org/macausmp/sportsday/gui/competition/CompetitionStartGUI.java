package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.List;
import java.util.Objects;

public class CompetitionStartGUI extends AbstractCompetitionGUI {
    public CompetitionStartGUI() {
        super(54, Component.translatable("gui.start.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(18, start(ELYTRA_RACING));
        getInventory().setItem(19, start(ICE_BOAT_RACING));
        getInventory().setItem(20, start(JAVELIN_THROW));
        getInventory().setItem(21, start(OBSTACLE_COURSE));
        getInventory().setItem(22, start(PARKOUR));
        getInventory().setItem(23, start(SUMO));
    }

    @ButtonHandler("event")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!Competitions.start(p, item.getItemMeta().getPersistentDataContainer().get(ItemUtil.EVENT_ID, PersistentDataType.STRING)))
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack start(@NotNull ItemStack event) {
        ItemStack clone = event.clone();
        List<Component> lore = Objects.requireNonNull(clone.lore());
        lore.add(Component.text(""));
        lore.add(TextUtil.text(Component.translatable("gui.start_competition")));
        clone.lore(lore);
        return clone;
    }
}
