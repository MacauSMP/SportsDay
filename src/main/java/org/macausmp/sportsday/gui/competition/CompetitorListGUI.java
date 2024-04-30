package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.Pageable;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.*;

public class CompetitorListGUI extends AbstractCompetitionGUI implements Pageable {
    private static final Set<CompetitorListGUI> HANDLER = new HashSet<>();
    private int page = 0;

    public CompetitorListGUI() {
        super(54, Component.translatable("gui.competitor_list.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, ItemUtil.addWrapper(GUIButton.COMPETITOR_LIST));
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(9, GUIButton.PREVIOUS_PAGE);
        getInventory().setItem(13, pages());
        getInventory().setItem(17, GUIButton.NEXT_PAGE);
        update();
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(13, pages());
        for (int i = getStartSlot(); i < getEndSlot(); i++) {
            getInventory().setItem(i, null);
        }
        for (int i = 0; i < getSize(); i++) {
            if (i >= Competitions.getCompetitors().size()) break;
            getInventory().setItem(i + getStartSlot(), icon(Competitions.getCompetitors().stream().sorted(Comparator.comparingInt(CompetitorData::getNumber)).toList().get(i + getPage() * getSize()).getUUID()));
        }
    }

    public static void updateGUI() {
        HANDLER.forEach(CompetitorListGUI::update);
    }

    @ButtonHandler("player_icon")
    public void profile(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        p.openInventory(new CompetitorProfileGUI(Competitions.getCompetitor(Objects.requireNonNull(meta.getOwningPlayer()).getUniqueId())).getInventory());
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        nextPage();
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
        previousPage();
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator").args(Component.text(getPage() + 1), Component.text(getMaxPage())).decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack icon(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        Component online = Component.translatable(player.isOnline() ? "competitor.online" : "competitor.offline");
        Component display = Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online);
        Component number = Component.translatable("competitor.number").args(Component.text(Competitions.getCompetitor(uuid).getNumber())).color(NamedTextColor.YELLOW);
        Component score = Component.translatable("competitor.score").args(Component.text(Competitions.getCompetitor(uuid).getScore())).color(NamedTextColor.YELLOW);
        Component detail = Component.translatable("gui.competitor_profile.detail").args(Component.text(player.getName())).color(NamedTextColor.YELLOW);
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, "player_icon", display, number, score, "", detail);
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(player));
        return icon;
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }

    public void nextPage() {
        if (getPage() < getMaxPage() - 1) page++;
        update();
    }

    public void previousPage() {
        if (getPage() > 0) page--;
        update();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getMaxPage() throws ArithmeticException {
        if (Competitions.getCompetitors().isEmpty()) return 1;
        int i = Competitions.getCompetitors().size();
        return i % getSize() == 0 ? i / getSize() : i / getSize() + 1;
    }

    @Override
    public int getStartSlot() {
        return 18;
    }

    @Override
    public int getEndSlot() {
        return 54;
    }
}
