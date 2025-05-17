package org.macausmp.sportsday.gui.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.event.SumoEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;
import java.util.Objects;

public class SumoEventGUI extends EventGUI<SumoEvent> {
    private static final NamespacedKey STAGE_NUMBER = new NamespacedKey(PLUGIN, "stage_number");
    private static final ItemStack NEXT_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page1", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page1", "gui.page.prev");
    private static final ItemStack NEXT_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page2", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page2", "gui.page.prev");
    private final PageBox<SumoEvent.SumoStage> stagePageBox;
    private SumoEvent.SumoStage selectedStage;
    private final PageBox<SumoEvent.SumoMatch> matchPageBox;

    public SumoEventGUI(@NotNull SumoEvent event) {
        super(event);
        this.stagePageBox = new PageBox<>(this, 28, 35, () -> List.of(event.getSumoStages()));
        this.selectedStage = event.getSumoStage();
        this.matchPageBox = new PageBox<>(this, 36, 45, () -> selectedStage.getMatchList());
        for (int i = 9; i < 18; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(27, PREVIOUS_PAGE1);
        getInventory().setItem(35, NEXT_PAGE1);
        getInventory().setItem(48, PREVIOUS_PAGE2);
        getInventory().setItem(50, NEXT_PAGE2);
        update();
    }

    @Override
    protected void update() {
        super.update();
        getInventory().setItem(49, pages());
        if (event.getSumoStage().getCurrentMatch() != null)
            getInventory().setItem(21, currentMatch(event.getSumoStage()));
        stagePageBox.updatePage(this::stage);
        matchPageBox.updatePage(this::match);
    }

    private @NotNull ItemStack currentMatch(@NotNull SumoEvent.SumoStage stage) {
        SumoEvent.SumoMatch match = stage.getCurrentMatch();
        return ItemUtil.item(Material.COD, "sumo_match", "gui.event.sumo.current_match",
                Component.translatable("gui.event.sumo.current_match.status").arguments(match.getStatus()),
                Component.translatable("gui.event.sumo.match.stage").arguments(stage),
                Component.translatable("gui.event.sumo.match.number").arguments(Component.text(match.getNumber())),
                Component.translatable("gui.event.sumo.match.players")
                        .arguments(match.getFirstPlayerName(), match.getSecondPlayerName()),
                Component.translatable("gui.event.sumo.match.result").arguments(!match.isEnd()
                        ? Component.translatable("gui.event.tbd")
                        : Component.translatable("gui.event.sumo.match.winner")
                        .arguments(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }

    private @NotNull ItemStack stage(@NotNull SumoEvent.SumoStage stage) {
        ItemStack stack = ItemUtil.item(stage.getIcon(), "sumo_stage",
                Component.translatable("gui.event.sumo.stage.title")
                        .arguments(stage.getStage() == SumoEvent.SumoStage.Stage.ELIMINATE
                                ? Component.translatable("{0} #" + stage.getNumber()).arguments(stage) : stage),
                "gui.event.sumo.stage.lore");
        stack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(STAGE_NUMBER, PersistentDataType.INTEGER, stage.getNumber());
            if (stage == selectedStage)
                meta.setEnchantmentGlintOverride(true);
        });
        return stack;
    }

    private @NotNull ItemStack match(@NotNull SumoEvent.SumoMatch match) {
        return ItemUtil.item(Material.COD, "sumo_match",
                Component.translatable("gui.event.sumo.match.number").arguments(Component.text(match.getNumber())),
                match.isSet()
                        ? Component.translatable("gui.event.sumo.match.players")
                        .arguments(match.getFirstPlayerName(), match.getSecondPlayerName())
                        : Component.translatable("gui.event.sumo.match.players")
                        .arguments(Component.translatable("gui.event.tbd"), Component.translatable("gui.event.tbd")),
                Component.translatable("gui.event.sumo.match.result").arguments(!match.isEnd()
                        ? Component.translatable("gui.event.tbd")
                        : Component.translatable("gui.event.sumo.match.winner")
                        .arguments(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(event.getSports().getDisplayItem());
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(matchPageBox.getPage() + 1), Component.text(matchPageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    @ButtonHandler("sumo_stage")
    public void selectStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer().get(STAGE_NUMBER, PersistentDataType.INTEGER));
        selectedStage = event.getSumoStages()[i - 1];
        p.playSound(UI_BUTTON_CLICK_SOUND);
        update();
    }

    @ButtonHandler("next_page1")
    public void nextStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        stagePageBox.nextPage();
    }

    @ButtonHandler("prev_page1")
    public void prevStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        stagePageBox.previousPage();
    }

    @ButtonHandler("next_page2")
    public void nextMatch(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        matchPageBox.nextPage();
    }

    @ButtonHandler("prev_page2")
    public void prevMatch(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        matchPageBox.previousPage();
    }
}
