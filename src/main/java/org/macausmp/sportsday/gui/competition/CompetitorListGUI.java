package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.IPageableGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CompetitorListGUI extends AbstractGUI implements IPageableGUI {
    private static final List<CompetitorListGUI> HANDLER = new ArrayList<>();
    private int page = 0;

    public CompetitorListGUI() {
        super(54, Component.translatable("gui.competitor_list.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, ItemUtil.addEffect(GUIButton.COMPETITOR_LIST));
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
            getInventory().setItem(i + getStartSlot(), icon(Competitions.getCompetitors().get(i + getPage() * getSize()).getUUID()));
        }
    }

    public static void updateGUI() {
        for (CompetitorListGUI gui : HANDLER) {
            gui.update();
        }
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, Player p, @NotNull ItemStack item) {
        if (event.getInventory().getHolder() instanceof CompetitorListGUI gui) {
            if (ItemUtil.equals(item, "player_icon")) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                p.openInventory(new CompetitorProfileGUI(Competitions.getCompetitor(Objects.requireNonNull(meta.getOwningPlayer()).getUniqueId())).getInventory());
                return;
            }
            if (ItemUtil.equals(item, GUIButton.NEXT_PAGE)) {
                p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                gui.nextPage();
            } else if (ItemUtil.equals(item, GUIButton.PREVIOUS_PAGE)) {
                p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                gui.previousPage();
            }
        }
    }

    private @NotNull ItemStack pages() {
        return ItemUtil.item(Material.YELLOW_STAINED_GLASS_PANE, null, Component.translatable("book.pageIndicator").args(Component.text(getPage() + 1), Component.text(getMaxPage())));
    }

    private @NotNull ItemStack icon(UUID uuid) {
        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        icon.editMeta(SkullMeta.class, meta -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Component online = Component.translatable(player.isOnline() ? "competitor.online" : "competitor.offline");
            meta.displayName(TextUtil.text(Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online)));
            meta.setOwningPlayer(player);
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("competitor.number").args(Component.text(Competitions.getCompetitor(uuid).getNumber()))).color(NamedTextColor.YELLOW));
            lore.add(TextUtil.text(Component.translatable("competitor.score").args(Component.text(Competitions.getCompetitor(uuid).getScore()))).color(NamedTextColor.YELLOW));
            lore.add(Component.text(""));
            lore.add(TextUtil.text(Component.translatable("gui.competitor_profile.detail").args(Component.text(player.getName()))).color(NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "player_icon");
        });
        return icon;
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
        return Competitions.getCompetitors().isEmpty() ? 1 : Competitions.getCompetitors().size() % getSize() == 0 ? Competitions.getCompetitors().size() / getSize() : Competitions.getCompetitors().size() / getSize() + 1;
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
