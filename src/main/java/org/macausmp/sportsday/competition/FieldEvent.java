package org.macausmp.sportsday.competition;

import org.bukkit.Material;

/**
 * Represents a field event
 */
public abstract non-sealed class FieldEvent extends SportingEvent implements Savable {
    public FieldEvent(String id, Material displayItem) {
        super(id, displayItem);
    }

    /**
     * Called when the match starts.
     */
    protected abstract void onMatchStart();

    /**
     * Called when the match ends.
     */
    protected abstract void onMatchEnd();

    /**
     * Called when entering the next match.
     */
    protected abstract void nextMatch();
}
