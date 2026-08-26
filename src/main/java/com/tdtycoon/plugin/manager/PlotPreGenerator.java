package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Pre-generates a configurable number of plots on server startup.
 * This avoids lag spikes when players join and prevents players from sharing plot space.
 */
public class PlotPreGenerator {

    private final TowerDefensePlugin plugin;

    public PlotPreGenerator(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Pre-generate plots asynchronously on startup.
     * Reads config for world and count.
     */
    public void preGeneratePlots() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("plots");
        if (config == null) {
            plugin.getLogger().warning("No plots config section found!");
            return;
        }

        String worldName = config.getString("world", "world");
        int plotsToGenerate = config.getInt("pre-generate-count", 100);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Plot world '" + worldName + "' not found!");
            return;
        }

        // Run on async thread to avoid lag
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int sizeX = config.getConfigurationSection("size").getInt("x");
            int sizeY = config.getConfigurationSection("size").getInt("y");
            int sizeZ = config.getConfigurationSection("size").getInt("z");
            int padding = config.getInt("padding", 10);
            int plotsPerRow = config.getInt("plots-per-row", 8);

            ConfigurationSection origin = config.getConfigurationSection("template-origin");
            int templateX = origin.getInt("x");
            int templateY = origin.getInt("y");
            int templateZ = origin.getInt("z");

            plugin.getLogger().info("Pre-generating " + plotsToGenerate + " plots...");

            for (int index = 1; index <= plotsToGenerate; index++) {
                // Calculate grid position
                int row = index / plotsPerRow;
                int col = index % plotsPerRow;
                int x = templateX + col * (sizeX + padding);
                int z = templateZ + row * (sizeZ + padding);

                Location destOrigin = new Location(world, x, templateY, z);
                copyTemplateInto(destOrigin, world, templateX, templateY, templateZ, sizeX, sizeY, sizeZ);

                if (index % 10 == 0) {
                    plugin.getLogger().info("Pre-generated " + index + " plots...");
                }
            }

            plugin.getLogger().info("Pre-generation complete!");
        });
    }

    /**
     * Copy the template plot to a new location.
     */
    private void copyTemplateInto(Location destOrigin, World world, int templateX, int templateY, int templateZ, int sizeX, int sizeY, int sizeZ) {
        Location src = new Location(world, templateX, templateY, templateZ);

        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    Block from = src.getWorld().getBlockAt(
                            src.getBlockX() + dx, src.getBlockY() + dy, src.getBlockZ() + dz);
                    Block to = destOrigin.getWorld().getBlockAt(
                            destOrigin.getBlockX() + dx, destOrigin.getBlockY() + dy, destOrigin.getBlockZ() + dz);
                    to.setBlockData(from.getBlockData(), false);
                }
            }
        }
    }
}
