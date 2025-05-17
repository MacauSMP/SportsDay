package org.macausmp.sportsday.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a page box for plugin gui.
 *
 * @param <T> type of entries
 */
public final class PageBox<T> {
    private final PluginGUI gui;
    private final int start;
    private final int end;
    private final Supplier<List<T>> entries;
    private final Sorter<T> sorter;
    private final Predicate<T> filter;
    private int page;

    /**
     * A page box with the specified gui and slot.
     *
     * @param gui the gui
     * @param start content item start slot
     * @param end content item end slot
     * @param entries entries
     */
    public PageBox(@NotNull PluginGUI gui, int start, int end, @NotNull Supplier<List<T>> entries) {
        this(gui, start, end, entries, null, t -> true);
    }

    /**
     * A page box with the specified gui and slot.
     *
     * @param gui the gui
     * @param start content item start slot
     * @param end content item end slot
     * @param entries entries
     * @param filter filter of entries
     */
    public PageBox(@NotNull PluginGUI gui, int start, int end, @NotNull Supplier<List<T>> entries,
                   @Nullable Sorter<T> sorter, @NotNull Filter<T, ?> filter) {
        this(gui, start, end, entries, sorter, filter::filter);
    }

    /**
     * A page box with the specified gui and slot.
     *
     * @param gui the gui
     * @param start content item start slot
     * @param end content item end slot
     * @param entries entries
     * @param filter filter of entries
     */
    public PageBox(@NotNull PluginGUI gui, int start, int end, @NotNull Supplier<List<T>> entries,
                   @Nullable Sorter<T> sorter, @NotNull Predicate<T> filter) {
        this.gui = gui;
        this.start = start;
        this.end = end;
        this.entries = entries;
        this.sorter = sorter;
        this.filter = filter;
    }

    /**
     * Gets the content item.
     *
     * @return content item
     */
    public List<T> getEntries() {
        return entries.get();
    }

    /**
     * Gets current page number.
     *
     * @return number of current page
     */
    public int getPage() {
        return page;
    }

    /**
     * Gets page count of gui.
     *
     * @return page count of gui
     */
    public int getMaxPage() {
        if (getEntries().isEmpty())
            return 1;
        double i = (double) getEntries().size() / getSize();
        return (int) (i == (int) i ? i : i + 1);
    }

    /**
     * Gets the slot where the content item starts.
     *
     * @return slot where the content item starts
     */
    public int getStartSlot() {
        return start;
    }

    /**
     * Gets the slot where the content item ends.
     *
     * @return slot where the content item ends
     */
    public int getEndSlot() {
        return end;
    }

    /**
     * Gets the size of content item.
     *
     * @return size of content item
     */
    public int getSize() {
        return end - start;
    }

    /**
     * Go to next page.
     */
    public void nextPage() {
        if (getPage() + 1 < getMaxPage())
            ++page;
        gui.update();
    }

    /**
     * Go to previous page.
     */
    public void previousPage() {
        if (getPage() > 0)
            --page;
        gui.update();
    }

    /**
     * Update gui content item
     *
     * @param function map each entry to {@link ItemStack}
     */
    public void updatePage(@NotNull Function<T, ItemStack> function) {
        for (int i = getStartSlot(); i < getEndSlot(); i++)
            gui.getInventory().setItem(i, null);
        List<T> entries = new ArrayList<>(getEntries().stream().filter(filter).toList());
        if (sorter != null)
            entries.sort(sorter.comparator());
        for (int i = 0; i < getSize(); i++) {
            if (i >= entries.size())
                break;
            gui.getInventory().setItem(i + getStartSlot(), function.apply(entries.get(i + getPage() * getSize())));
        }
    }
}
