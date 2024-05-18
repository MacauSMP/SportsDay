package org.macausmp.sportsday.competition;

public interface ITrackEvent extends IEvent {
    /**
     * Get the number of laps required to complete.
     * @return number of laps required to complete
     */
    int getMaxLaps();
}
