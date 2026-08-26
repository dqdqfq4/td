package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final TowerDefensePlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> loadOrCreate(id));
    }

    private PlayerData loadOrCreate(UUID uuid) {
        if (file.exists()) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection s = yml.getConfigurationSection(uuid.toString());
            if (s != null) {
                PlayerData data = new PlayerData(uuid);
                data.setCoins(s.getLong("coins"));
                data.setSpaceUsed(s.getInt("space-used"));
                data.setMaxSpace(s.getInt("max-space"));
                data.setPrestige(s.getInt("prestige"));
                data.setMaxCampHealth(s.getDouble("max-camp-health"));
                data.setCampHealth(s.getDouble("camp-health"));
                data.setAttacksPaused(s.getBoolean("attacks-paused", true));
                data.setPlotIndex(s.getInt("plot-index", -1));
                return data;
            }
        }
        PlayerData fresh = new PlayerData(uuid);
        ConfigurationSection econ = plugin.getConfig().getConfigurationSection("economy");
        if (econ != null) {
            fresh.setCoins(econ.getLong("starting-coins"));
            fresh.setMaxSpace(econ.getInt("starting-max-space"));
            fresh.setMaxCampHealth(econ.getDouble("starting-max-camp-health"));
            fresh.setCampHealth(fresh.getMaxCampHealth());
        }
        return fresh;
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;

        YamlConfiguration yml = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        String base = uuid.toString();
        yml.set(base + ".coins", data.getCoins());
        yml.set(base + ".space-used", data.getSpaceUsed());
        yml.set(base + ".max-space", data.getMaxSpace());
        yml.set(base + ".prestige", data.getPrestige());
        yml.set(base + ".max-camp-health", data.getMaxCampHealth());
        yml.set(base + ".camp-health", data.getCampHealth());
        yml.set(base + ".attacks-paused", data.isAttacksPaused());
        yml.set(base + ".plot-index", data.getPlotIndex());

        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save playerdata.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID id : cache.keySet()) save(id);
    }
}
