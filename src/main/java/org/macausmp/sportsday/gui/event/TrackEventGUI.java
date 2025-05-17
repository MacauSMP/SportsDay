package org.macausmp.sportsday.gui.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.event.TrackEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

public class TrackEventGUI<T extends TrackEvent> extends EventGUI<T> {
    private final PageBox<ContestantData> pageBox;

    public TrackEventGUI(@NotNull T event) {
        super(event);
        this.pageBox = new PageBox<>(this, 18, 45,
                () -> event.getContestants().stream()
                        .sorted((d1, d2) -> event.getRecord(d1) > 0 && event.getRecord(d2) > 0
                                ? Float.compare(event.getRecord(d1), event.getRecord(d2)) : 0).toList());
        for (int i = 9; i < 18; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(2, laps());
        getInventory().setItem(3, checkpoints());
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        update();
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::icon);
    }

    private @NotNull ItemStack laps() {
        ItemStack stack = ItemUtil.item(Material.LEAD, null,
                Component.translatable("event.track.laps")
                        .arguments(Component.text(event.getLaps())).color(NamedTextColor.YELLOW));
        stack.setAmount(event.getLaps());
        return stack;
    }

    private @NotNull ItemStack checkpoints() {
        return ItemUtil.item(Material.RED_BED, null,
                Component.translatable("event.track.all_checkpoints_required")
                        .arguments(Component.text(event.areAllCheckpointsRequired())).color(NamedTextColor.YELLOW));
    }

    private @NotNull ItemStack icon(@NotNull ContestantData data) {
        float f = event.getRecord(data);
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "icon",
                Component.translatable("gui.event.track.player")
                        .arguments(Component.text(data.getName())),
                Component.translatable("gui.event.track.result").arguments(f == -1
                        ? Component.translatable("gui.event.tbd")
                        : Component.translatable("gui.event.track.second").arguments(Component.text(f))));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }

    private @NotNull ItemStack pages() {
        return ItemUtil.item(event.getSports().getDisplayItem(), null,
                Component.translatable("book.pageIndicator")
                        .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage())));
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
