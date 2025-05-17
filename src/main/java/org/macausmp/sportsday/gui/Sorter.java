package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents a sorter for page box.
 *
 * @param <E> type of entries
 */
public final class Sorter<E> {
    private int counter = 0;
    private final Entry<E>[] entries;

    @SafeVarargs
    public Sorter(@NotNull Entry<E> @NotNull ... entries) {
        if (entries.length < 1) {
            throw new IllegalArgumentException("Requiring at least one element");
        }
        this.entries = entries;
    }

    public void next() {
        if (++counter >= entries.length)
            counter = 0;
    }

    public void prev() {
        if (--counter < 0)
            counter = entries.length - 1;
    }

    public Comparator<E> comparator() {
        return entries[counter].comparator;
    }

    public @NotNull ItemStack sorterItem(@NotNull Material material, @NotNull String id) {
        Component[] lore = Arrays.stream(entries)
                .map(e -> Component.translatable("- %s")
                        .arguments(Component.translatable(e.content)).color(NamedTextColor.GRAY))
                .toArray(Component[]::new);
        lore[counter] = lore[counter].color(NamedTextColor.YELLOW);
        return ItemUtil.item(material, id, "gui.sorter", (Object[]) lore);
    }

    public static class Entry<T> {
        private final String content;
        private final Comparator<T> comparator;

        public Entry(@NotNull String content, @NotNull Comparator<T> comparator) {
            this.content = content;
            this.comparator = comparator;
        }
    }
}
