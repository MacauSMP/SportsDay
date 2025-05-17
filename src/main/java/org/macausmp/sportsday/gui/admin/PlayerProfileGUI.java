package org.macausmp.sportsday.gui.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.gui.PermissionRequired;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.training.SportsTrainingHandler;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@PermissionRequired
public class PlayerProfileGUI extends PluginGUI {
    private final Scoreboard scoreboard = PLUGIN.getServer().getScoreboardManager().getMainScoreboard();
    private final OfflinePlayer player;

    public PlayerProfileGUI(@NotNull OfflinePlayer player) {
        super(54, Component.translatable("gui.player_profile.title")
                .arguments(Component.text(Objects.requireNonNull(player.getName()))));
        this.player = player;
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        update();
    }

    @Override
    protected void update() {
        getInventory().setItem(22, icon());
        boolean b = SportsDay.isContestant(player);
        getInventory().setItem(15, b ? stats() : null);
        getInventory().setItem(16, b ? score() : null);
        getInventory().setItem(25, b ? unregister() : null);
        getInventory().setItem(24, player.isOnline() ? tp() : null);
    }

    public static void updateAll(@NotNull UUID uuid) {
        updateAll(PlayerProfileGUI.class, gui -> gui.player.getUniqueId().equals(uuid));
    }

    private @NotNull ItemStack icon() {
        boolean online = player.isOnline();
        Component display = Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)")
                .arguments(Component.translatable(online ? "player.online" : "player.offline"));
        List<Component> lore = new ArrayList<>();
        Team team = scoreboard.getPlayerTeam(player);
        lore.add(team != null ? team.displayName().color(team.color()) : Component.translatable("gui.text.none"));
        if (online) {
            Component status;
            if (SportsTrainingHandler.getTrainingSport(player.getUniqueId()) != null) {
                status = Component.translatable("player.status.training");
            } else if (SportsDay.getCurrentEvent() != null && SportsDay.getCurrentEvent().getContestants()
                    .contains(SportsDay.getContestant(player.getUniqueId()))) {
                status = Component.translatable("player.status.competition");
            } else {
                status = Component.translatable("player.status.free");
            }
            lore.add(Component.translatable("player.status").arguments(status));
        }
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, "player_icon", display, lore.toArray());
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(player));
        return icon;
    }

    private @NotNull ItemStack stats() {
        ContestantData data = SportsDay.getContestant(player.getUniqueId());
        Component number = Component.translatable("contestant.number")
                .arguments(Component.text(data.getNumber()))
                .color(NamedTextColor.YELLOW);
        Component score = Component.translatable("contestant.score")
                .arguments(Component.text(data.getScore()))
                .color(NamedTextColor.YELLOW);
        return ItemUtil.item(Material.PAPER, "stats", "gui.player_profile.stats", number, score);
    }

    private @NotNull ItemStack score() {
        ContestantData data = SportsDay.getContestant(player.getUniqueId());
        return ItemUtil.item(Material.GOLD_INGOT,"score",
                Component.translatable("contestant.score")
                        .arguments(Component.text(data.getScore())).color(NamedTextColor.YELLOW),
                "gui.player_profile.score_lore1", "gui.player_profile.score_lore2");
    }

    private @NotNull ItemStack tp() {
        return ItemUtil.item(Material.ENDER_PEARL, "tp", "gui.player_profile.tp");
    }

    private @NotNull ItemStack unregister() {
        return ItemUtil.item(Material.NAME_TAG, "unregister", "gui.player_profile.unregister");
    }

    @ButtonHandler("tp")
    public void tp(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.teleport(Objects.requireNonNull(player.getPlayer()));
        p.playSound(TELEPORT_SOUND);
    }

    @ButtonHandler("score")
    public void score(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        ContestantData data = SportsDay.getContestant(player.getUniqueId());
        if (data.isRemoved()) {
            p.sendMessage(Component.translatable("command.competition.unregister.failed")
                    .arguments(Component.text(data.getName())).color(NamedTextColor.RED));
            p.closeInventory();
            return;
        }
        data.addScore(e.isLeftClick() ? 1 : e.isRightClick() ? -1 : 0);
        p.playSound(UI_BUTTON_CLICK_SOUND);
        update();
    }

    @ButtonHandler("unregister")
    public void unregister(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ConfirmationGUI(this, player -> {
            SportsDay.leave(this.player);
            return this;
        }).open(p);
        updateAll(player.getUniqueId());
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new PlayersListGUI().open(p);
    }
}
