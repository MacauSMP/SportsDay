package org.macausmp.sportsday.gui.setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.SportsSelectGUI;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.sport.Setting;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;

public class SportSettingsGUI extends PluginGUI {
    protected final Sport sport;

    public SportSettingsGUI(@NotNull Sport sport) {
        super(27, Component.translatable("gui.sport_settings.title").arguments(sport.asComponent().color(NamedTextColor.BLACK)));
        this.sport = sport;
        for (int i = 18; i < 27; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(26, BACK);
        update();
    }

    @Override
    protected void update() {
        getInventory().setItem(0, enable());
        getInventory().setItem(1, amount());
    }

    private @NotNull ItemStack enable() {
        return ItemUtil.item(
                sport.getSetting(Sport.Settings.ENABLE) ? Material.LIME_DYE : Material.BARRIER,
                "enable",
                Component.translatable("gui.sport_settings.enable").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(sport.getSetting(Sport.Settings.ENABLE) ? "gui.enabled" : "gui.disabled")),
                "gui.toggle");
    }

    private @NotNull ItemStack amount() {
        int amount = sport.getSetting(Sport.Settings.LEAST_PLAYERS_REQUIRED);
        ItemStack stack = ItemUtil.item(
                Material.PLAYER_HEAD,
                "amount",
                Component.translatable("gui.sport_settings.amount").arguments(Component.text(amount)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease");
        stack.setAmount(amount);
        return stack;
    }

    protected void setBoolean(@NotNull Setting<Boolean> setting, @NotNull Player p) {
        boolean value = sport.getSetting(setting);
        sport.setSetting(setting, !value);
        p.playSound(value ? EXECUTION_SUCCESS_SOUND : EXECUTION_FAIL_SOUND);
    }

    protected void setInteger(@NotNull Setting<Integer> setting, @NotNull Player p, @NotNull ClickType type) {
        if (!type.isLeftClick() && !type.isRightClick())
            return;
        boolean add = type.isLeftClick();
        sport.setSetting(setting, Math.max(sport.getSetting(setting) + (add ? 1 : -1), 1));
        p.playSound(add ? EXECUTION_SUCCESS_SOUND : EXECUTION_FAIL_SOUND);
    }

    @ButtonHandler("enable")
    public void enable(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setBoolean(Sport.Settings.ENABLE, p);
    }

    @ButtonHandler("amount")
    public void amount(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        setInteger(Sport.Settings.LEAST_PLAYERS_REQUIRED, p, e.getClick());
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new SportsSelectGUI(new AdminMenuGUI(), sport -> p.openInventory(sport.getSettingGUI().getInventory())).open(p);
    }
}
