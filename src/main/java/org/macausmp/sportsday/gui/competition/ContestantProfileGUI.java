package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ContestantData;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Objects;
import java.util.UUID;

public class ContestantProfileGUI extends AbstractCompetitionGUI {
    private final ContestantData data;

    public ContestantProfileGUI(@NotNull ContestantData data) {
        super(54, Component.translatable("gui.contestant_profile.title").arguments(Component.text(data.getName())));
        this.data = data;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, COMPETITION_CONSOLE);
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        getInventory().setItem(26, unregister());
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(18, icon());
        getInventory().setItem(19, increase());
        getInventory().setItem(20, decrease());
    }

    public static void updateProfile(UUID uuid) {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof ContestantProfileGUI)
                .map(inv -> (ContestantProfileGUI) inv.getHolder())
                .filter(gui -> gui.data.getUUID().equals(uuid)).forEach(ContestantProfileGUI::update);
    }

    @ButtonHandler("increase")
    public void increase(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (data.isRemoved()) {
            p.sendMessage(Component.translatable("command.competition.unregister.failed")
                    .arguments(Component.text(data.getName())).color(NamedTextColor.RED));
            p.closeInventory();
            return;
        }
        data.addScore(e.isLeftClick() ? 1 : e.isRightClick() ? 5 : 0);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("decrease")
    public void decrease(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (data.isRemoved()) {
            p.sendMessage(Component.translatable("command.competition.unregister.failed")
                    .arguments(Component.text(data.getName())).color(NamedTextColor.RED));
            p.closeInventory();
            return;
        }
        data.addScore(e.isLeftClick() ? -1 : e.isRightClick() ? -5 : 0);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("unregister")
    public void unregister(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new ConfirmationGUI(this, player -> {
            boolean b = Competitions.leave(data.getPlayer());
            player.sendMessage(b
                    ? Component.translatable("command.competition.unregister.success")
                    .arguments(Component.text(data.getName())).color(NamedTextColor.GREEN)
                    : Component.translatable("command.competition.unregister.failed")
                    .arguments(Component.text(data.getName())).color(NamedTextColor.RED));
            return b;
        }).getInventory());
    }

    private @NotNull ItemStack icon() {
        Component online = Component.translatable(data.isOnline() ? "contestant.online" : "contestant.offline");
        Component display = Component.translatable(Objects.requireNonNull(data.getName()) + " (%s)").arguments(online);
        Component number = Component.translatable("contestant.number").arguments(Component.text(data.getNumber()))
                .color(NamedTextColor.YELLOW);
        Component score = Component.translatable("contestant.score").arguments(Component.text(data.getScore()))
                .color(NamedTextColor.YELLOW);
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, null, display, number, score);
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return icon;
    }

    private @NotNull ItemStack unregister() {
        Component display = Component.translatable("gui.contestant.unregister")
                .arguments(Component.text(data.getName())).color(NamedTextColor.RED);
        Component lore = Component.translatable("gui.contestant.unregister_lore");
        return ItemUtil.item(Material.RED_CONCRETE, "unregister", display, lore);
    }

    private @NotNull ItemStack increase() {
        Component score = Component.translatable("contestant.score").arguments(Component.text(data.getScore()))
                .color(NamedTextColor.YELLOW);
        return ItemUtil.item(Material.YELLOW_STAINED_GLASS_PANE,"increase", "gui.contestant.increase",
                score, "gui.contestant.increase_lore1", "gui.contestant.increase_lore2");
    }

    private @NotNull ItemStack decrease() {
        Component score = Component.translatable("contestant.score").arguments(Component.text(data.getScore()))
                .color(NamedTextColor.YELLOW);
        return ItemUtil.item(Material.RED_STAINED_GLASS_PANE, "decrease", "gui.contestant.decrease",
                score, "gui.contestant.decrease_lore1", "gui.contestant.decrease_lore2");
    }
}
