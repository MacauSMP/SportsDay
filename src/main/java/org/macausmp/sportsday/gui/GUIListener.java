package org.macausmp.sportsday.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.CompetitionSettingsGUI;
import org.macausmp.sportsday.gui.competition.CompetitionStartGUI;
import org.macausmp.sportsday.gui.competition.CompetitorListGUI;
import org.macausmp.sportsday.util.ItemUtil;

public final class GUIListener implements Listener {
    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (!p.isOp()) e.setCancelled(true);
            if (e.getInventory().getHolder() instanceof AbstractGUI gui) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item != null && ItemUtil.hasID(item) && e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
                    if (ItemUtil.equals(item, GUIButton.COMPETITION_INFO)) {
                        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                        p.openInventory(new CompetitionInfoGUI().getInventory());
                        return;
                    } else if (ItemUtil.equals(item, GUIButton.COMPETITOR_LIST)) {
                        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                        p.openInventory(new CompetitorListGUI().getInventory());
                        return;
                    } else if (ItemUtil.equals(item, GUIButton.START_COMPETITION)) {
                        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStage() != Stage.ENDED) {
                            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
                            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
                            return;
                        }
                        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                        p.openInventory(new CompetitionStartGUI().getInventory());
                        return;
                    } else if (ItemUtil.equals(item, GUIButton.END_COMPETITION)) {
                        boolean b = Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStage() == Stage.ENDED;
                        p.playSound(Sound.sound(Key.key(b ? "minecraft:entity.enderman.teleport" : "minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                        Competitions.forceEnd(p);
                        return;
                    } else if (ItemUtil.equals(item, GUIButton.COMPETITION_SETTINGS)) {
                        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                        p.openInventory(new CompetitionSettingsGUI().getInventory());
                        return;
                    }
                    gui.onClick(e, p, item);
                }
            }
        }
    }
}
