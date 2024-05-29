package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoMatch;
import org.macausmp.sportsday.competition.sumo.SumoStage;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.List;
import java.util.Objects;

public class SumoGUI extends AbstractEventGUI<Sumo> {
    private static final NamespacedKey STAGE_NUMBER = new NamespacedKey(PLUGIN, "stage_number");
    private static final ItemStack NEXT_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page1", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page1", "gui.page.prev");
    private static final ItemStack NEXT_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page2", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page2", "gui.page.prev");
    private final PageBox<SumoStage> stagePageBox;
    private SumoStage selectedStage;
    private final PageBox<SumoMatch> matchPageBox;

    public SumoGUI(@NotNull Sumo event) {
        super(54, Component.translatable("event.name.sumo"), event);
        this.stagePageBox = new PageBox<>(this, 28, 35, () -> List.of(event.getSumoStages()));
        this.selectedStage = event.getSumoStage();
        this.matchPageBox = new PageBox<>(this, 45, 54, () -> selectedStage.getMatchList());
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 36, BOARD);
        getInventory().setItem(27, PREVIOUS_PAGE1);
        getInventory().setItem(35, NEXT_PAGE1);
        getInventory().setItem(36, PREVIOUS_PAGE2);
        getInventory().setItem(44, NEXT_PAGE2);
        update();
    }

    @Override
    public void update() {
        if (Competitions.getCurrentEvent() != event)
            return;
        HANDLER.add(this);
        if (event.getSumoStage().getCurrentMatch() != null)
            getInventory().setItem(18, currentMatch(event.getSumoStage()));
        stagePageBox.updatePage(this::stage);
        matchPageBox.updatePage(this::match);
    }

    @ButtonHandler("sumo_stage")
    public void selectStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer().get(STAGE_NUMBER, PersistentDataType.INTEGER));
        selectedStage = event.getSumoStages()[i - 1];
        update();
    }

    @ButtonHandler("next_page1")
    public void nextStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        stagePageBox.nextPage();
    }

    @ButtonHandler("prev_page1")
    public void prevStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        stagePageBox.previousPage();
    }

    @ButtonHandler("next_page2")
    public void nextMatch(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        matchPageBox.nextPage();
    }

    @ButtonHandler("prev_page2")
    public void prevMatch(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        matchPageBox.previousPage();
    }

    private @NotNull ItemStack currentMatch(@NotNull SumoStage stage) {
        SumoMatch match = stage.getCurrentMatch();
        return ItemUtil.item(Material.COD, "sumo_match", "gui.competition.sumo.current_match",
                Component.translatable("gui.competition.sumo.current_match.status").arguments(match.getStatus().getName()),
                Component.translatable("gui.competition.sumo.match.stage").arguments(stage.getName()),
                Component.translatable("gui.competition.sumo.match.number").arguments(Component.text(match.getNumber())),
                Component.translatable("gui.competition.sumo.match.players")
                        .arguments(match.getFirstPlayerName(), match.getSecondPlayerName()),
                Component.translatable("gui.competition.sumo.match.result").arguments(!match.isEnd()
                        ? Component.translatable("gui.competition.tbd")
                        : Component.translatable("gui.competition.sumo.match.winner")
                        .arguments(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }

    private @NotNull ItemStack stage(@NotNull SumoStage stage) {
        ItemStack stack = ItemUtil.item(stage.getIcon(), "sumo_stage",
                Component.translatable("gui.competition.sumo.stage.title").arguments(stage.getStage() == SumoStage.Stage.ELIMINATE
                        ? Component.translatable("{0} #" + stage.getNumber()).arguments(stage.getName())
                        : stage.getName()),
                "gui.competition.sumo.stage.lore");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(STAGE_NUMBER, PersistentDataType.INTEGER, stage.getNumber()));
        if (stage == selectedStage) {
            stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        }
        return stack;
    }

    private @NotNull ItemStack match(@NotNull SumoMatch match) {
        return ItemUtil.item(Material.COD, "sumo_match",
                Component.translatable("gui.competition.sumo.match.number").arguments(Component.text(match.getNumber())),
                match.isSet()
                        ? Component.translatable("gui.competition.sumo.match.players")
                        .arguments(match.getFirstPlayerName(), match.getSecondPlayerName())
                        : Component.translatable("gui.competition.sumo.match.players")
                        .arguments(Component.translatable("gui.competition.tbd"), Component.translatable("gui.competition.tbd")),
                Component.translatable("gui.competition.sumo.match.result").arguments(!match.isEnd()
                        ? Component.translatable("gui.competition.tbd")
                        : Component.translatable("gui.competition.sumo.match.winner")
                        .arguments(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }
}
