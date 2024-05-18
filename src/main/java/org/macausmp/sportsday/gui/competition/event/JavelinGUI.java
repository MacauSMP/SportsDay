package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.JavelinThrow;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ContestantData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class JavelinGUI extends AbstractCompetitionGUI {
    private static final Set<JavelinGUI> HANDLER = new HashSet<>();
    private final JavelinThrow event;
    private final PageBox<ContestantData> pageBox;

    public JavelinGUI(@NotNull JavelinThrow event) {
        super(54, Component.translatable("event.name.javelin_throw"));
        this.event = event;
        this.pageBox = new PageBox<>(this, 36, 54, () -> event.getContestants().stream().toList());
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, BOARD);
            getInventory().setItem(i + 27, BOARD);
        }
        getInventory().setItem(0, ItemUtil.addWrapper(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(27, PREVIOUS_PAGE);
        getInventory().setItem(35, NEXT_PAGE);
        update();
    }

    @Override
    public void update() {
        HANDLER.add(this);
        if (event.getCurrentPlayer() != null)
            getInventory().setItem(18, currentMatch());
        pageBox.updatePage(this::match);
    }

    public static void updateGUI() {
        HANDLER.forEach(JavelinGUI::update);
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        pageBox.nextPage();
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        pageBox.previousPage();
    }

    private @NotNull ItemStack currentMatch() {
        return ItemUtil.item(Material.COD, "sumo_match",
                Component.translatable("gui.competition.javelin.current_player")
                        .args(Component.text(event.getCurrentPlayer().getName())));
    }

    private @NotNull ItemStack match(@NotNull ContestantData data) {
        JavelinThrow.ScoreResult sr = event.getScoreResult(data.getUUID());
        return ItemUtil.item(Material.COD, "sumo_match",
                Component.translatable("gui.competition.javelin.player")
                        .args(Component.text(data.getName())),
                Component.translatable("gui.competition.javelin.result")
                        .args(sr == null
                                ? Component.translatable("gui.competition.tbd")
                                : Component.translatable("gui.competition.javelin.meters")
                                .args(Component.text(sr.getDistance()))));
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }
}
