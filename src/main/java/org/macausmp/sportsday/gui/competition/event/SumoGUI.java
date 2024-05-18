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
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SumoGUI extends AbstractCompetitionGUI {
    private static final Set<SumoGUI> HANDLER = new HashSet<>();
    private static final NamespacedKey STAGE_NUMBER = new NamespacedKey(PLUGIN, "stage_number");
    private static final ItemStack NEXT_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page1", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE1 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page1", "gui.page.prev");
    private static final ItemStack NEXT_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page2", "gui.page.next");
    private static final ItemStack PREVIOUS_PAGE2 = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page2", "gui.page.prev");
    private final Sumo event;
    private final PageBox<SumoStage> stagePageBox;
    private SumoStage selectedStage;
    private final PageBox<SumoMatch> matchPageBox;

    public SumoGUI(@NotNull Sumo event) {
        super(54, Component.translatable("event.name.sumo"));
        this.event = event;
        this.stagePageBox = new PageBox<>(this, 28, 35, () -> List.of(event.getSumoStages()));
        this.selectedStage = event.getSumoStage();
        this.matchPageBox = new PageBox<>(this, 45, 54, () -> selectedStage.getMatchList());
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, BOARD);
            getInventory().setItem(i + 36, BOARD);
        }
        getInventory().setItem(0, ItemUtil.addWrapper(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
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

    public static void updateGUI() {
        HANDLER.forEach(SumoGUI::update);
    }

    @ButtonHandler("sumo_stage")
    public void selectStage(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        Integer i = item.getItemMeta().getPersistentDataContainer().get(STAGE_NUMBER, PersistentDataType.INTEGER);
        if (i != null)
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
                Component.translatable("gui.competition.sumo.current_match.status").args(match.getStatus().getName()),
                Component.translatable("gui.competition.sumo.match.stage").args(stage.getName()),
                Component.translatable("gui.competition.sumo.match.number").args(Component.text(match.getNumber())),
                Component.translatable("gui.competition.sumo.match.players")
                        .args(match.getPlayers()[0].displayName(), match.getPlayers()[1].displayName()),
                Component.translatable("gui.competition.sumo.match.result")
                        .args(!match.isEnd()
                                ? Component.translatable("gui.competition.tbd")
                                : Component.translatable("gui.competition.sumo.match.winner")
                                .args(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }

    private @NotNull ItemStack stage(@NotNull SumoStage stage) {
        ItemStack stack = ItemUtil.item(stage.getIcon(), "sumo_stage",
                Component.translatable("gui.competition.sumo.stage.title")
                        .args(stage.getStage() == SumoStage.Stage.ELIMINATE
                                ? Component.translatable("{0} #" + stage.getNumber()).args(stage.getName())
                                : stage.getName()));
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(STAGE_NUMBER, PersistentDataType.INTEGER, stage.getNumber()));
        if (stage == selectedStage) {
            stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        }
        return stack;
    }

    private @NotNull ItemStack match(@NotNull SumoMatch match) {
        return ItemUtil.item(Material.COD, "sumo_match",
                Component.translatable("gui.competition.sumo.match.number").args(Component.text(match.getNumber())),
                match.isSet()
                        ? Component.translatable("gui.competition.sumo.match.players").args(match.getPlayers()[0].displayName(), match.getPlayers()[1].displayName())
                        : Component.translatable("gui.competition.sumo.match.players").args(Component.translatable("gui.competition.tbd"), Component.translatable("gui.competition.tbd")),
                Component.translatable("gui.competition.sumo.match.result")
                        .args(!match.isEnd()
                                ? Component.translatable("gui.competition.tbd")
                                : Component.translatable("gui.competition.sumo.match.winner")
                                .args(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName())));
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }
}
