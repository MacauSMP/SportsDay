package org.macausmp.sportsday.gui;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a page box for plugin gui.
 */
public final class PageBox<T> {
    private final PluginGUI gui;
    private final int start;
    private final int end;
    private final Supplier<List<T>> entries;
    private int page;

    /**
     * A page box with the specified gui and slot.
     * @param gui the gui
     * @param start content item start slot
     * @param end content item end slot
     */
    public PageBox(PluginGUI gui, int start, int end, Supplier<List<T>> entries) {
        this.gui = gui;
        this.start = start;
        this.end = end;
        this.entries = entries;
    }

    /**
     * Gets the content item.
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
     * @return size of content item
     */
    public int getSize() {
        return end - start;
    }

    /**
     * Go to next page.
     */
    public void nextPage() {
        if (getPage() < getMaxPage() - 1)
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

    public void updatePage(Function<T, ItemStack> function) {
        for (int i = getStartSlot(); i < getEndSlot(); i++)
            gui.getInventory().setItem(i, null);
        for (int i = 0; i < getSize(); i++) {
            if (i >= getEntries().size())
                break;
            gui.getInventory().setItem(i + getStartSlot(), function.apply(getEntries().get(i + getPage() * getSize())));
        }
    }
}
