package com.tdtycoon.plugin.model;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;

/**
 * A grid-allocated region in the shared world belonging to one player.
 * All gameplay coordinates (path waypoints, camp spot, tower placement bounds)
 * are stored as offsets from the plot origin and translated to world
 * coordinates on demand, so every plot behaves identically.
 */
public class Plot {

    private final int index;
    private final UUID owner;
    private final Location origin; // world-space anchor (bottom-north-west corner)

    // filled from config.yml, relative to origin
    private List<double[]> pathWaypointsRelative;
    private double[] campSpotRelative;
    private double[] boundsMinRelative;
    private double[] boundsMaxRelative;

    public Plot(int index, UUID owner, Location origin) {
        this.index = index;
        this.owner = owner;
        this.origin = origin;
    }

    public int getIndex() { return index; }
    public UUID getOwner() { return owner; }
    public Location getOrigin() { return origin; }
    public World getWorld() { return origin.getWorld(); }

    public List<double[]> getPathWaypointsRelative() { return pathWaypointsRelative; }
    public void setPathWaypointsRelative(List<double[]> pathWaypointsRelative) { this.pathWaypointsRelative = pathWaypointsRelative; }

    public double[] getCampSpotRelative() { return campSpotRelative; }
    public void setCampSpotRelative(double[] campSpotRelative) { this.campSpotRelative = campSpotRelative; }

    public double[] getBoundsMinRelative() { return boundsMinRelative; }
    public void setBoundsMinRelative(double[] boundsMinRelative) { this.boundsMinRelative = boundsMinRelative; }

    public double[] getBoundsMaxRelative() { return boundsMaxRelative; }
    public void setBoundsMaxRelative(double[] boundsMaxRelative) { this.boundsMaxRelative = boundsMaxRelative; }

    /** Converts a relative offset [x,y,z] to an absolute world Location. */
    public Location toWorld(double[] relative) {
        return origin.clone().add(relative[0], relative[1], relative[2]);
    }

    public Location getCampLocation() {
        return toWorld(campSpotRelative);
    }
}
