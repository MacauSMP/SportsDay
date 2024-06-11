package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ContestantData;
import org.macausmp.sportsday.competition.JavelinThrow;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

public class JavelinGUI extends AbstractEventGUI<JavelinThrow> {
    private final PageBox<ContestantData> pageBox;

    public JavelinGUI(@NotNull JavelinThrow event) {
        super(54, Component.translatable("event.name.javelin_throw"), event);
        this.pageBox = new PageBox<>(this, 36, 54, () -> event.getContestants().stream().toList());
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 27, BOARD);
        getInventory().setItem(27, PREVIOUS_PAGE);
        getInventory().setItem(35, NEXT_PAGE);
        update();
    }

    @Override
    public void update() {
        if (Competitions.getCurrentEvent() != event)
            return;
        if (event.getCurrentPlayer() != null)
            getInventory().setItem(18, current());
        pageBox.updatePage(this::icon);
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

    private @NotNull ItemStack current() {
        ContestantData data = event.getCurrentPlayer();
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "current",
                Component.translatable("gui.competition.javelin.current_player")
                        .arguments(Component.text(data.getName())));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }

    private @NotNull ItemStack icon(@NotNull ContestantData data) {
        JavelinThrow.ScoreResult sr = event.getScoreResult(data.getUUID());
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "icon",
                Component.translatable("gui.competition.javelin.player").arguments(Component.text(data.getName())),
                Component.translatable("gui.competition.javelin.result").arguments(sr == null || !sr.isSet()
                        ? Component.translatable("gui.competition.tbd")
                        : Component.translatable("gui.competition.javelin.meters").arguments(Component.text(sr.getDistance()))));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }
}
