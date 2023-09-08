package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionInfoGUI extends AbstractGUI {
    private static final List<CompetitionInfoGUI> HANDLER = new ArrayList<>();

    public CompetitionInfoGUI() {
        super(36, Component.translatable("gui.info.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, ItemUtil.addEffect(GUIButton.COMPETITION_INFO));
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(18, status());
        getInventory().setItem(27, player());
    }

    public static void updateGUI() {
        for (CompetitionInfoGUI gui : HANDLER) {
            gui.update();
        }
    }


    @Override
    public void onClick(InventoryClickEvent e, Player p, ItemStack item) {
    }

    private @NotNull ItemStack status() {
        boolean b = Competitions.getCurrentEvent() != null;
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentEvent().getName() : Component.translatable("gui.none"));
        Component lore = Component.translatable("competition.stage").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentEvent().getStage().getName() : Stage.IDLE.getName());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.competitors.total").color(NamedTextColor.GREEN).args(Component.text(Competitions.getCompetitors().size()).color(NamedTextColor.YELLOW));
        Component lore = Component.translatable("competition.competitors.online").color(NamedTextColor.GREEN).args(Component.text(Competitions.getOnlineCompetitors().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display, lore);
    }
}
