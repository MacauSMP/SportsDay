package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ContestantData;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class ContestantsListGUI extends AbstractCompetitionGUI {
    private final PageBox<ContestantData> pageBox = new PageBox<>(this, 18, 54,
            () -> Competitions.getContestants().stream().sorted(Comparator.comparingInt(ContestantData::getNumber)).toList());

    public ContestantsListGUI() {
        super(54, Component.translatable("gui.contestants_list.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, ItemUtil.setGlint(CONTESTANTS_LIST));
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(9, PREVIOUS_PAGE);
        getInventory().setItem(13, pages());
        getInventory().setItem(17, NEXT_PAGE);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(13, pages());
        pageBox.updatePage(i -> icon(i.getUUID()));
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof ContestantsListGUI)
                .map(inv -> (ContestantsListGUI) inv.getHolder())
                .forEach(ContestantsListGUI::update);
    }

    @ButtonHandler("player_icon")
    public void profile(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        UUID uuid = Objects.requireNonNull(((SkullMeta) item.getItemMeta()).getOwningPlayer()).getUniqueId();
        p.openInventory(new ContestantProfileGUI(Competitions.getContestant(uuid)).getInventory());
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

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack icon(UUID uuid) {
        ContestantData data = Competitions.getContestant(uuid);
        Component online = Component.translatable(data.isOnline() ? "contestant.online" : "contestant.offline");
        Component display = Component.translatable(Objects.requireNonNull(data.getName()) + " (%s)").arguments(online);
        Component number = Component.translatable("contestant.number").arguments(Component.text(data.getNumber()))
                .color(NamedTextColor.YELLOW);
        Component score = Component.translatable("contestant.score").arguments(Component.text(data.getScore()))
                .color(NamedTextColor.YELLOW);
        Component detail = Component.translatable("gui.contestant_profile.detail")
                .arguments(Component.text(data.getName())).color(NamedTextColor.YELLOW);
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, "player_icon", display,
                number, score, "", detail);
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return icon;
    }
}
