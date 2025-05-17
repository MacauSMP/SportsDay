package org.macausmp.sportsday.gui.admin;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;

import java.util.Objects;

@PermissionRequired
public class EventLoadGUI extends PluginGUI {
    private final PageBox<PersistentDataContainer> pageBox = new PageBox<>(this, 0, 45,
            SportsDay::getSavedEvents);

    public EventLoadGUI() {
        super(54, Component.translatable("gui.load.title"));
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(45, clear());
        update();
    }

    @Override
    protected void update() {
        pageBox.updatePage(this::save);
    }

    private @NotNull ItemStack save(@NotNull PersistentDataContainer save) {
        Sport sport = SportsRegistry.SPORT.get(Objects.requireNonNull(save
                .get(new NamespacedKey(PLUGIN, "sport"), KeyDataType.KEY_DATA_TYPE)));
        if (sport == null) {
            return ItemUtil.item(Material.BARRIER, null, "gui.load.failed");
        }

        ItemStack stack = ItemUtil.item(sport.getDisplayItem(), "save", sport, "gui.load.lore");
        stack.editMeta(meta -> meta.getPersistentDataContainer()
                .set(new NamespacedKey(PLUGIN, "data"), PersistentDataType.TAG_CONTAINER, save));
        return stack;
    }

    private @NotNull ItemStack clear() {
        return ItemUtil.item(Material.CAULDRON, "clear", "gui.load.clear.title", "gui.load.clear.lore");
    }

    @ButtonHandler("save")
    public void save(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        PersistentDataContainer data = Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(PLUGIN, "data"), PersistentDataType.TAG_CONTAINER));
        SportsDay.loadEvent(p, data);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("clear")
    public void clear(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new ConfirmationGUI(this, player -> {
            SportsDay.clearSavedEvents();
            p.playSound(Sound.sound(Key.key("minecraft:item.bundle.drop_contents"), Sound.Source.MASTER, 1f, 1f));
            updateAll();
            return this;
        }).open(p);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        new EventStartGUI().open(p);
    }
}
