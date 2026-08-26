package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages visual elements for plots: path blocks, camp blocks, and spawn point particles.
 * Particles only display when the plot owner is online.
 */
public class PlotVisualManager {

    private final TowerDefensePlugin plugin;
    private final Map<UUID, BukkitTask> particleTasks = new HashMap<>();
    private static final Material PATH_MATERIAL = Material.LIME_CONCRETE;
    private static final Material CAMP_MATERIAL = Material.RED_CONCRETE;

    public PlotVisualManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Build physical blocks for a plot's path and camp when it's created.
     */
    public void buildPlotLayout(Plot plot) {
        // Build path blocks
        for (double[] waypoint : plot.getPathWaypointsRelative()) {
            Location loc = plot.toWorld(waypoint);
            Block block = loc.getBlock();
            block.setType(PATH_MATERIAL);
        }

        // Build camp block
        Location campLoc = plot.getCampLocation();
        Block campBlock = campLoc.getBlock();
        campBlock.setType(CAMP_MATERIAL);
    }

    /**
     * Start particle effects at the spawn point for this plot (first waypoint).
     * Called when the plot owner joins.
     */
    public void startSpawnParticles(UUID playerId, Plot plot) {
        if (particleTasks.containsKey(playerId)) {
            return; // Already running
        }

        double[] firstWaypoint = plot.getPathWaypointsRelative().get(0);
        Location spawnLoc = plot.toWorld(firstWaypoint);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (spawnLoc.getWorld() != null) {
                // Create a circle of particles around the spawn point
                spawnParticleCircle(spawnLoc, 1.5, 8);
            }
        }, 0L, 10L); // Run every 10 ticks (0.5 seconds)

        particleTasks.put(playerId, task);
    }

    /**
     * Stop particle effects for this plot (called when player logs out).
     */
    public void stopSpawnParticles(UUID playerId) {
        BukkitTask task = particleTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Create a circle of particles around a location.
     */
    private void spawnParticleCircle(Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(center.getWorld(), x, center.getY() + 1, z);

            center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Clear all running particle tasks (e.g., on plugin disable).
     */
    public void shutdown() {
        for (BukkitTask task : particleTasks.values()) {
            task.cancel();
        }
        particleTasks.clear();
    }
}
