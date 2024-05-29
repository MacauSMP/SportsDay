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
import org.macausmp.sportsday.competition.ITrackEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

public class TrackEventGUI extends AbstractEventGUI<ITrackEvent> {
    private final PageBox<ContestantData> pageBox;

    public TrackEventGUI(@NotNull ITrackEvent event) {
        super(54, Component.translatable("event.name." + event.getID()), event);
        this.pageBox = new PageBox<>(this, 18, 54,
                () -> event.getContestants().stream()
                        .sorted((d1, d2) -> event.getRecord(d1) > 0 && event.getRecord(d2) > 0
                                ? Float.compare(event.getRecord(d1), event.getRecord(d2)) : 0).toList());
        getInventory().setItem(9, PREVIOUS_PAGE);
        getInventory().setItem(17, NEXT_PAGE);
        update();
    }

    @Override
    public void update() {
        if (Competitions.getCurrentEvent() != event)
            return;
        HANDLER.add(this);
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

    private @NotNull ItemStack icon(@NotNull ContestantData data) {
        float f = event.getRecord(data);
        ItemStack stack = ItemUtil.item(Material.PLAYER_HEAD, "icon",
                Component.translatable("gui.competition.track.player")
                        .arguments(Component.text(data.getName())),
                Component.translatable("gui.competition.track.result").arguments(f == -1
                        ? Component.translatable("gui.competition.tbd")
                        : Component.translatable("gui.competition.track.second").arguments(Component.text(f))));
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return stack;
    }
}
