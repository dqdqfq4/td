package com.tdtycoon.plugin.listener;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;
import com.tdtycoon.plugin.model.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final TowerDefensePlugin plugin;

    public PlayerJoinListener(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        Plot plot = plugin.getPlotManager().getOrCreatePlot(player.getUniqueId(), data.getPlotIndex());
        if (data.getPlotIndex() < 0) {
            data.setPlotIndex(plot.getIndex());
        }

        // Build visual elements (path blocks, camp blocks) if not already done
        plugin.getPlotVisualManager().buildPlotLayout(plot);

        // Start particle effects at spawn point
        plugin.getPlotVisualManager().startSpawnParticles(player.getUniqueId(), plot);

        player.teleport(plot.getOrigin().clone().add(2, 1, 2));
        player.sendMessage("§aWelcome back! Your plot is #" + plot.getIndex() + ".");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().save(player.getUniqueId());
        plugin.getPlotManager().unloadPlot(player.getUniqueId());

        // Stop particle effects when player logs out
        plugin.getPlotVisualManager().stopSpawnParticles(player.getUniqueId());
    }
}
