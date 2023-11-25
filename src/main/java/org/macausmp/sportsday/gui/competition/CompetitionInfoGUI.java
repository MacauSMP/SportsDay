package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class CompetitionInfoGUI extends AbstractCompetitionGUI {
    private static final Set<CompetitionInfoGUI> HANDLER = new HashSet<>();

    public CompetitionInfoGUI() {
        super(36, Component.translatable("gui.info.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, ItemUtil.addEffect(GUIButton.COMPETITION_INFO));
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, GUIButton.START_COMPETITION);
        getInventory().setItem(19, GUIButton.END_COMPETITION);
        update();
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(27, status());
        getInventory().setItem(28, player());
    }

    public static void updateGUI() {
        HANDLER.forEach(PluginGUI::update);
    }

    @Override
    public void onClick(@NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, GUIButton.START_COMPETITION)) {
            if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
                p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
                p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
                return;
            }
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            p.openInventory(new CompetitionStartGUI().getInventory());
        } else if (ItemUtil.equals(item, GUIButton.END_COMPETITION)) {
            boolean b = Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED;
            p.playSound(Sound.sound(Key.key(b ? "minecraft:entity.enderman.teleport" : "minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            Competitions.forceEnd(p);
        }
    }

    private @NotNull ItemStack status() {
        boolean b = Competitions.getCurrentEvent() != null;
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentEvent().getName() : Component.translatable("gui.none"));
        Component lore = Component.translatable("competition.status").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentEvent().getStatus().getName() : Status.IDLE.getName());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.competitors.total").color(NamedTextColor.GREEN).args(Component.text(Competitions.getCompetitors().size()).color(NamedTextColor.YELLOW));
        Component lore = Component.translatable("competition.competitors.online").color(NamedTextColor.GREEN).args(Component.text(Competitions.getOnlineCompetitors().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display, lore);
    }
}
