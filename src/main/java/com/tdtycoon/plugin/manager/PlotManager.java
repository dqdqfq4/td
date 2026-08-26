package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Allocates each player a plot on a shared-world grid and physically copies
 * the template plot's blocks into new slots the first time they're needed,
 * so every plot has an identical arena (path, walls, camp) without WorldEdit.
 */
public class PlotManager {

    private final TowerDefensePlugin plugin;
    private final Map<UUID, Plot> activePlots = new HashMap<>();
    private final Set<Integer> usedIndices = new HashSet<>();

    private World world;
    private int templateX, templateY, templateZ;
    private int sizeX, sizeY, sizeZ;
    private int padding;
    private int plotsPerRow;

    private List<double[]> pathWaypointsRelative;
    private double[] campSpotRelative;
    private double[] boundsMinRelative;
    private double[] boundsMaxRelative;

    public PlotManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection plots = plugin.getConfig().getConfigurationSection("plots");
        String worldName = plots.getString("world", "world");
        this.world = Bukkit.getWorld(worldName);

        ConfigurationSection origin = plots.getConfigurationSection("template-origin");
        templateX = origin.getInt("x");
        templateY = origin.getInt("y");
        templateZ = origin.getInt("z");

        ConfigurationSection size = plots.getConfigurationSection("size");
        sizeX = size.getInt("x");
        sizeY = size.getInt("y");
        sizeZ = size.getInt("z");

        padding = plots.getInt("padding", 10);
        plotsPerRow = plots.getInt("plots-per-row", 8);

        ConfigurationSection layout = plugin.getConfig().getConfigurationSection("layout");
        pathWaypointsRelative = new ArrayList<>();
        for (Object o : layout.getList("path-waypoints")) {
            List<?> l = (List<?>) o;
            pathWaypointsRelative.add(new double[]{
                    ((Number) l.get(0)).doubleValue(),
                    ((Number) l.get(1)).doubleValue(),
                    ((Number) l.get(2)).doubleValue()
            });
        }
        campSpotRelative = toArray(layout.getDoubleList("camp-spot"));
        boundsMinRelative = toArray(layout.getDoubleList("bounds-min"));
        boundsMaxRelative = toArray(layout.getDoubleList("bounds-max"));
    }

    private double[] toArray(List<Double> list) {
        return new double[]{list.get(0), list.get(1), list.get(2)};
    }

    /** Grid index -> world-space origin of that plot slot. */
    private Location indexToOrigin(int index) {
        int row = index / plotsPerRow;
        int col = index % plotsPerRow;
        double x = templateX + col * (sizeX + padding);
        double z = templateZ + row * (sizeZ + padding);
        return new Location(world, x, templateY, z);
    }

    private Location templateOrigin() {
        return new Location(world, templateX, templateY, templateZ);
    }

    /**
     * Returns the player's plot, allocating and (if needed) physically
     * copying the template arena into a fresh grid slot on first use.
     */
    public Plot getOrCreatePlot(UUID playerId, int assignedIndex) {
        if (activePlots.containsKey(playerId)) {
            return activePlots.get(playerId);
        }

        int index = assignedIndex;
        if (index < 0) {
            index = nextFreeIndex();
        }
        usedIndices.add(index);

        Location origin = indexToOrigin(index);
        if (index != 0) {
            copyTemplateInto(origin);
        }

        Plot plot = new Plot(index, playerId, origin);
        plot.setPathWaypointsRelative(pathWaypointsRelative);
        plot.setCampSpotRelative(campSpotRelative);
        plot.setBoundsMinRelative(boundsMinRelative);
        plot.setBoundsMaxRelative(boundsMaxRelative);

        activePlots.put(playerId, plot);
        return plot;
    }

    private int nextFreeIndex() {
        int i = 0;
        while (usedIndices.contains(i)) i++;
        return i;
    }

    /**
     * Block-by-block copy of the template plot's bounding box into a new
     * plot slot. Runs on the main thread; fine for moderate plot sizes.
     * For large arenas, consider chunking this across ticks.
     */
    private void copyTemplateInto(Location destOrigin) {
        Location src = templateOrigin();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    Block from = src.getWorld().getBlockAt(
                            src.getBlockX() + dx, src.getBlockY() + dy, src.getBlockZ() + dz);
                    Block to = destOrigin.getWorld().getBlockAt(
                            destOrigin.getBlockX() + dx, destOrigin.getBlockY() + dy, destOrigin.getBlockZ() + dz);
                    BlockState fromState = from.getState();
                    to.setBlockData(fromState.getBlockData(), false);
                }
            }
        }
    }

    public Plot getActivePlot(UUID playerId) {
        return activePlots.get(playerId);
    }

    public void unloadPlot(UUID playerId) {
        activePlots.remove(playerId);
        // Note: index stays reserved in usedIndices for this session; persisted
        // per-player in PlayerData#plotIndex so returning players get the same slot.
    }
}
