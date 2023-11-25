package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Objects;
import java.util.UUID;

public class CompetitorProfileGUI extends AbstractCompetitionGUI {
    private final CompetitorData data;

    public CompetitorProfileGUI(@NotNull CompetitorData data) {
        super(54, Component.translatable("gui.competitor_profile.title").args(Component.text(data.getName())));
        this.data = data;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, icon(data.getUUID()));
        getInventory().setItem(19, unregister(data));
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(@NotNull Player p, @NotNull ItemStack item) {
        if (ItemUtil.equals(item, unregister(data))) {
            p.sendMessage(Competitions.leave(data.getPlayer()) ? Component.translatable("command.competition.unregister.success.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.GREEN) : Component.translatable("command.competition.unregister.failed.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.RED));
            p.closeInventory();
        }
    }

    private @NotNull ItemStack icon(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        Component online = Component.translatable(player.isOnline() ? "competitor.online" : "competitor.offline");
        Component display = Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online);
        Component number = Component.translatable("competitor.number").args(Component.text(Competitions.getCompetitor(uuid).getNumber())).color(NamedTextColor.YELLOW);
        Component score = Component.translatable("competitor.score").args(Component.text(Competitions.getCompetitor(uuid).getScore())).color(NamedTextColor.YELLOW);
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, null, display, number, score);
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(player));
        return icon;
    }

    private @NotNull ItemStack unregister(CompetitorData data) {
        Component display = Component.translatable("gui.competitor.unregister").args(Component.text(data.getName())).color(NamedTextColor.RED);
        Component lore = Component.translatable("gui.competitor.unregister_lore");
        return ItemUtil.item(Material.RED_CONCRETE, "unregister", display, lore);
    }
}
