package org.macausmp.sportsday.gui.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.event.JavelinThrowEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

public class JavelinThrowEventGUI extends EventGUI<JavelinThrowEvent> {
    private final PageBox<ContestantData> pageBox;

    public JavelinThrowEventGUI(@NotNull JavelinThrowEvent event) {
        super(event);
        for (int i = 9; i < 18; i++)
            getInventory().setItem(i, BOARD);
        this.pageBox = new PageBox<>(this, 18, 45, () -> event.getContestants().stream().toList());
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        update();
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(49, pages());
        if (event.getCurrentContestant() != null)
            getInventory().setItem(21, current());
        pageBox.updatePage(this::icon);
    }

    private @NotNull ItemStack current() {
        ContestantData data = event.getCurrentContestant();
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "current",
                Component.translatable("gui.event.javelin.current_player")
                        .arguments(Component.text(data.getName())));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }

    private @NotNull ItemStack icon(@NotNull ContestantData data) {
        JavelinThrowEvent.ScoreResult sr = event.getScoreResult(data.getUUID());
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "icon",
                Component.translatable("gui.event.javelin.player").arguments(Component.text(data.getName())),
                Component.translatable("gui.event.javelin.result").arguments(sr == null || !sr.isSet()
                        ? Component.translatable("gui.event.tbd")
                        : Component.translatable("gui.event.javelin.meters").arguments(Component.text(sr.getDistance()))));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(event.getSports().getDisplayItem());
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        pageBox.nextPage();
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        pageBox.previousPage();
    }
}
