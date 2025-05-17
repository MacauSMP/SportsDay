package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Represents a filter for page box.
 *
 * @param <E> the type of entries
 * @param <T> the type of the input to the predicate
 */
public final class Filter<E, T> {
    private final BiPredicate<T, E> predicate;
    private final Entry<T>[] entries;
    private int counter = 0;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public Filter(@NotNull String defaultEntry, @NotNull BiPredicate<T, E> predicate, Entry<T> @NotNull ... entries) {
        this.predicate = predicate;
        this.entries = (Entry<T>[]) new Entry[entries.length + 1];
        this.entries[0] = new Entry<>(defaultEntry, null);
        System.arraycopy(entries, 0, this.entries, 1, entries.length);
    }

    public void next() {
        if (++counter >= entries.length)
            counter = 0;
    }

    public void prev() {
        if (--counter < 0)
            counter = entries.length - 1;
    }

    public boolean filter(@NotNull E entry) {
        T t = entries[counter].t;
        return t == null || predicate.test(t, entry);
    }

    public @NotNull ItemStack filterItem(@NotNull Material material, @NotNull String id) {
        Component[] lore = Arrays.stream(entries)
                .map(e -> Component.translatable("- %s")
                        .arguments(Component.translatable(e.content)).color(NamedTextColor.GRAY))
                .toArray(Component[]::new);
        lore[counter] = lore[counter].color(NamedTextColor.YELLOW);
        return ItemUtil.item(material, id, "gui.filter", (Object[]) lore);
    }

    public static class Entry<T> {
        private final String content;
        private final T t;

        public Entry(@NotNull String content, @Nullable T t) {
            this.content = content;
            this.t = t;
        }
    }
}
