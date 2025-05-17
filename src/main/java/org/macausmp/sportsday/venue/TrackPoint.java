package org.macausmp.sportsday.venue;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TrackPoint {
    private Location location;
    private Vector corner1 = new Vector();
    private Vector corner2 = new Vector();
    private final BoundingBox boundingBox;

    public TrackPoint(Location location, BoundingBox boundingBox) {
        this.location = location;
        this.boundingBox = boundingBox;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setBoundingBoxCorner1(Vector vector) {
        this.corner1 = vector;
        this.boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    public void setBoundingBoxCorner2(Vector vector) {
        this.corner2 = vector;
        this.boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean overlaps(@NotNull Entity entity) {
        return boundingBox.overlaps(entity.getBoundingBox());
    }
}
