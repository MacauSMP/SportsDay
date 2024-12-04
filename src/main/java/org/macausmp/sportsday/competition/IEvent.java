package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;

/**
 * Represents a competition event
 */
public interface IEvent extends Keyed, Listener {
    /**
     * Get the event's id from the config file.
     *
     * @return id of event
     */
    String getID();

    /**
     * Get the event's name from the config file.
     *
     * @return name of event
     */
    Component getName();

    /**
     * Get the event's minimum required number of players from the config file.
     *
     * @return minimum required number of players of event
     */
    int getLeastPlayersRequired();

    /**
     * Get the event's location from the config file.
     *
     * @return location of event
     */
    Location getLocation();

    /**
     * Get the event's world.
     *
     * @return world of event
     */
    World getWorld();

    /**
     * Return {@code True} if event is enabled.
     *
     * @return {@code True} if event is enabled
     */
    boolean isEnable();

    /**
     * Get the current event status.
     *
     * @return current status of event
     */
    Status getStatus();

    /**
     * Return {@code True} if event is paused.
     *
     * @return {@code True} if event is paused
     */
    boolean isPaused();

    /**
     * Get the last event time.
     *
     * @return the time of the last event
     */
    long getLastTime();

    /**
     * Gets a view of {@link ContestantData} of current event.
     *
     * @return a view of {@link ContestantData} of current event
     */
    Collection<ContestantData> getContestants();

    /**
     * Get the leaderboard of event.
     *
     * @return leaderboard of event
     */
    List<ContestantData> getLeaderboard();

    /**
     * Set up the event and make it get ready to start.
     */
    void setup();

    /**
     * Start the event.
     */
    void start();

    /**
     * End the event.
     */
    void end();

    /**
     * Pause the event.
     */
    void pause();

    /**
     * Unpause the event.
     */
    void unpause();

    /**
     * Terminate the event.
     */
    void terminate();

    /**
     * Disqualification of contestant.
     *
     * @param contestant who is going to be disqualified
     */
    void onDisqualification(ContestantData contestant);

    /**
     * Teleport player to event location and sets up practice environment for the player.
     *
     * @param player who is going to practice this event
     */
    void joinPractice(Player player);
}
