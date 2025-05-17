package org.macausmp.sportsday.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
    public static final NamespacedKey ITEM_ID = new NamespacedKey(PLUGIN, "item_id");
    public static final NamespacedKey EVENT_ID = new NamespacedKey(PLUGIN, "event_id");
    public static final NamespacedKey BIND = new NamespacedKey(PLUGIN, "bind");
    public static final ItemStack OP_BOOK = setGlint(item(Material.KNOWLEDGE_BOOK, "competition_book", "item.op_book", "item.op_book_lore"));
    public static final ItemStack MENU = setBind(item(Material.COMPASS, "menu", "item.menu", "item.menu_lore"));
    public static final ItemStack LEAVE_TRAINING = setBind(item(Material.BARRIER, "leave_training", "item.leave_training", "item.leave_training_lore"));
    public static final ItemStack CHECKPOINT = setBind(item(Material.BEACON, "training_checkpoint", "item.training_checkpoint", "item.training_checkpoint_lore"));
    public static final ItemStack RESET = setBind(item(Material.RED_BED, "training_reset", "item.training_reset", "item.training_reset_lore"));
    public static final ItemStack CUSTOMIZE = setBind(item(Material.CHEST, "customize", "item.customize", "item.customize_lore1", "item.customize_lore2"));
    public static final ItemStack SPRAY = setBind(item(Material.DRAGON_BREATH, "graffiti_spray", "item.spray", "item.spray_lore1", "item.spray_lore2", "item.spray_lore3"));
    public static final String START = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0";
    public static final String PAUSE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc3YWFlMWEyNmI5NTI0OTNhNzM3MWMzMGFkOGM0OTFmMTJiNTc0Y2M5NGE0MWIyZjkxYTM3M2NhNjhmOTA5OCJ9fX0=";
    public static final String PLUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=";
    public static final String ONE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=";
    public static final String TWO = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19";

    @Contract(pure = true)
    public static @NotNull ItemStack item(@NotNull ItemStack stack, String id, Object display, Object... lore) {
        ItemStack clone = stack.clone();
        clone.editMeta(meta -> {
            if (display != null) {
                if (display instanceof ComponentLike like)
                    meta.displayName(TextUtil.text(like));
                else if (display instanceof String s)
                    meta.displayName(TextUtil.text(Component.translatable(s)));
                else
                    throw new IllegalArgumentException("Object type must be ComponentLike or String");
            }
            if (lore != null) {
                List<Component> components = new ArrayList<>();
                for (Object o : lore) {
                    if (o instanceof ComponentLike like)
                        components.add(TextUtil.text(like));
                    else if (o instanceof String s)
                        components.add(TextUtil.text(Component.translatable(s)));
                    else
                        throw new IllegalArgumentException("Object type must be ComponentLike or String");
                }
                meta.lore(components);
            }
            if (id != null)
                meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, id);
        });
        return clone;
    }

    @Contract(pure = true)
    public static @NotNull ItemStack item(@NotNull Material material, String id, Object display, Object... lore) {
        return item(new ItemStack(material), id, display, lore);
    }

    @Contract(pure = true)
    public static @NotNull ItemStack head(String value, String id, Object display, Object... lore) {
        ItemStack stack = item(Material.PLAYER_HEAD, id, display, lore);
        if (value == null || value.isEmpty())
            return stack;
        stack.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add(new ProfileProperty("textures", value));
            meta.setPlayerProfile(profile);
        });
        return stack;
    }

    @Contract(pure = true)
    public static @NotNull ItemStack setGlint(@NotNull ItemStack stack) {
        ItemStack s = stack.clone();
        s.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return s;
    }

    @Contract(pure = true)
    public static @NotNull ItemStack hideTooltip(@NotNull ItemStack stack) {
        ItemStack s = stack.clone();
        s.editMeta(meta -> meta.setHideTooltip(true));
        return s;
    }

    @Contract(pure = true)
    public static boolean isBind(@NotNull ItemStack stack) {
        if (!stack.hasItemMeta())
            return false;
        return Boolean.TRUE.equals(stack.getItemMeta().getPersistentDataContainer().get(BIND, PersistentDataType.BOOLEAN));
    }

    @Contract(pure = true)
    public static @NotNull ItemStack setBind(@NotNull ItemStack stack) {
        ItemStack s = stack.clone();
        s.editMeta(meta -> meta.getPersistentDataContainer().set(BIND, PersistentDataType.BOOLEAN, true));
        return s;
    }

    @Contract(pure = true)
    public static boolean hasID(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().has(ITEM_ID, PersistentDataType.STRING);
    }

    @Contract(pure = true)
    public static String getID(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
    }

    @Contract(pure = true)
    public static boolean equals(@NotNull ItemStack stack, @NotNull ItemStack other) {
        if (!stack.hasItemMeta() || !other.hasItemMeta())
            return false;
        String id1 = stack.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
        String id2 = other.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
        if (id1 == null || id2 == null)
            return false;
        return Objects.equals(id1, id2);
    }

    @Contract(pure = true)
    public static boolean equals(@NotNull ItemStack stack, @NotNull String key) {
        if (!stack.hasItemMeta())
            return false;
        String id = stack.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id, key);
    }
}
