package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks camp health per player and the "repair clicker" progress
 * (N clicks = +1 health), matching the video's Camp Panel GUI.
 */
public class CampManager {

    private final TowerDefensePlugin plugin;
    private final Map<UUID, Integer> repairClickProgress = new HashMap<>();

    public CampManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    public void damageCamp(UUID owner, double amount) {
        PlayerData data = plugin.getPlayerDataManager().get(owner);
        data.setCampHealth(data.getCampHealth() - amount);
        if (data.getCampHealth() <= 0) {
            onCampDestroyed(owner);
        }
    }

    private void onCampDestroyed(UUID owner) {
        // Wave fully breached: pause attacks until the player repairs/resets.
        PlayerData data = plugin.getPlayerDataManager().get(owner);
        data.setAttacksPaused(true);
        plugin.getWaveManager().clearWave(owner);
    }

    /** One click toward repairing the camp. Returns true if a health point was granted. */
    public boolean registerRepairClick(UUID owner) {
        int clicksNeeded = plugin.getConfig().getInt("economy.camp-repair-clicks-per-point", 2);
        int progress = repairClickProgress.getOrDefault(owner, 0) + 1;

        if (progress >= clicksNeeded) {
            repairClickProgress.put(owner, 0);
            PlayerData data = plugin.getPlayerDataManager().get(owner);
            data.setCampHealth(data.getCampHealth() + 1);
            return true;
        }
        repairClickProgress.put(owner, progress);
        return false;
    }

    public double getRepairProgressPercent(UUID owner) {
        int clicksNeeded = plugin.getConfig().getInt("economy.camp-repair-clicks-per-point", 2);
        int progress = repairClickProgress.getOrDefault(owner, 0);
        return (progress / (double) clicksNeeded) * 100.0;
    }
}
