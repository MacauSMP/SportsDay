package org.macausmp.sportsday.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public final class ItemUtil {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final NamespacedKey ITEM_ID = Objects.requireNonNull(NamespacedKey.fromString("item_id", PLUGIN));
    public static final NamespacedKey EVENT_ID = Objects.requireNonNull(NamespacedKey.fromString("event_id", PLUGIN));
    public static final ItemStack OP_BOOK = addEffect(item(Material.KNOWLEDGE_BOOK, "competition_book", "item.op_book", "item.op_book_lore"));
    public static final ItemStack MENU = item(Material.COMPASS, "menu", "item.menu", "item.menu_lore");
    public static final ItemStack QUIT_PRACTICE = item(Material.BARRIER, "quit_practice", "item.quit_practice", "item.quit_practice_lore");
    public static final ItemStack CUSTOMIZE = item(Material.CHEST, "customize", "item.customize", "item.customize_lore1", "item.customize_lore2");
    public static final ItemStack SPRAY = item(Material.DRAGON_BREATH, "graffiti_spray", "item.spray", "item.spray_lore1", "item.spray_lore2", "item.spray_lore3");
    public static final String START = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0";

    public static @NotNull ItemStack item(@NotNull ItemStack stack, String id, Object display, Object... lore) {
        ItemStack clone = stack.clone();
        clone.editMeta(meta -> {
            if (display != null) {
                if (display instanceof Component c) {
                    meta.displayName(TextUtil.text(c));
                } else if (display instanceof String s) {
                    meta.displayName(TextUtil.text(Component.translatable(s)));
                } else {
                    throw new IllegalArgumentException("Object type must be Component or String");
                }
            }
            if (lore != null) {
                List<Component> components = new ArrayList<>();
                for (Object o : lore) {
                    if (o instanceof Component c) {
                        components.add(TextUtil.text(c));
                    } else if (o instanceof String s) {
                        components.add(TextUtil.text(Component.translatable(s)));
                    } else {
                        throw new IllegalArgumentException("Object type must be Component or String");
                    }
                }
                meta.lore(components);
            }
            if (id != null) meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, id);
        });
        return clone;
    }

    public static @NotNull ItemStack item(Material material, String id, Object display, Object... lore) {
        return item(new ItemStack(material), id, display, lore);
    }

    public static @NotNull ItemStack head(String value, String id, Object display, Object... lore) {
        ItemStack stack = item(Material.PLAYER_HEAD, id, display, lore);
        if (value == null || value.isEmpty()) return stack;
        stack.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add(new ProfileProperty("textures", value));
            meta.setPlayerProfile(profile);
        });
        return stack;
    }

    @Contract("_ -> param1")
    public static @NotNull ItemStack addEffect(@NotNull ItemStack stack) {
        ItemStack s = stack.clone();
        s.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        s.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return s;
    }

    public static boolean hasID(@NotNull ItemStack button) {
        return button.hasItemMeta() && button.getItemMeta().getPersistentDataContainer().has(ItemUtil.ITEM_ID, PersistentDataType.STRING);
    }

    public static boolean equals(@NotNull ItemStack button, @NotNull ItemStack button2) {
        if (!button.hasItemMeta() || !button2.hasItemMeta()) return false;
        String id1 = button.getItemMeta().getPersistentDataContainer().get(ItemUtil.ITEM_ID, PersistentDataType.STRING);
        String id2 = button2.getItemMeta().getPersistentDataContainer().get(ItemUtil.ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id1, id2);
    }

    public static boolean equals(@NotNull ItemStack button, @NotNull String key) {
        if (!button.hasItemMeta()) return false;
        String id = button.getItemMeta().getPersistentDataContainer().get(ItemUtil.ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id, key);
    }
}
