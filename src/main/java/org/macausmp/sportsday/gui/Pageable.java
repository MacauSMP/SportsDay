package org.macausmp.sportsday.gui;

public interface Pageable {
    /**
     * Get current page number
     * @return number of current page
     */
    int getPage();

    /**
     * Get page count of gui
     * @return page count of gui
     */
    int getMaxPage();

    /**
     * Get the slot where the content item starts
     * @return slot where the content item starts
     */
    int getStartSlot();

    /**
     * Get the slot where the content item ends
     * @return slot where the content item ends
     */
    int getEndSlot();

    /**
     * Get the size of content item
     * @return size of content item
     */
    default int getSize() {
        return getEndSlot() - getStartSlot();
    }
}
