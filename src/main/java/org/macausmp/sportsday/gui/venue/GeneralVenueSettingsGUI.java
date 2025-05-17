package org.macausmp.sportsday.gui.venue;

import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Venue;

public class GeneralVenueSettingsGUI<V extends Venue> extends VenueSettingsGUI<V> {
    public GeneralVenueSettingsGUI(@NotNull Sport sport, @NotNull V venue) {
        super(36, sport, venue);
        update();
    }
}
