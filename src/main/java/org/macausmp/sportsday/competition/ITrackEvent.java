package org.macausmp.sportsday.competition;

public interface ITrackEvent extends IEvent {
    /**
     * Get the number of laps required to complete.
     * @return number of laps required to complete
     */
    int getMaxLaps();

    /**
     * Get the record of a specified contestant.
     * @return record of a specified contestant
     */
    float getRecord(ContestantData data);
}
