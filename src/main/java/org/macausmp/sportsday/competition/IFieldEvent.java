package org.macausmp.sportsday.competition;

public interface IFieldEvent extends IEvent {
    /**
     * Called when the match starts.
     */
    void onMatchStart();

    /**
     * Called when the match ends.
     */
    void onMatchEnd();

    /**
     * Called when entering the next match.
     */
    void nextMatch();
}
