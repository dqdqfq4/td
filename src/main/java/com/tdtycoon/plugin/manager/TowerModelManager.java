package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.RelativeBlock;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Loads/saves every tower type ever registered with /create_tower, and the
 * shop list of which models are currently purchasable.
 */
public class TowerModelManager {

    private final TowerDefensePlugin plugin;
    private final File file;
    private final Map<String, TowerModel> models = new LinkedHashMap<>();
    private final Set<String> shopModelIds = new LinkedHashSet<>();
    // material -> modelId, set via /settoweritem, used when a placed item is right-clicked into the world
    private final Map<String, String> itemToModel = new HashMap<>();

    public TowerModelManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "towers.yml");
        load();
    }

    public void registerModel(TowerModel model) {
        models.put(model.getModelId(), model);
        save();
    }

    public TowerModel getModel(String modelId) {
        return models.get(modelId);
    }

    public Collection<TowerModel> getAllModels() {
        return models.values();
    }

    public void addToShop(String modelId) {
        shopModelIds.add(modelId);
        save();
    }

    public void removeFromShop(String modelId) {
        shopModelIds.remove(modelId);
        save();
    }

    public List<TowerModel> getShopModels() {
        List<TowerModel> list = new ArrayList<>();
        for (String id : shopModelIds) {
            TowerModel m = models.get(id);
            if (m != null) list.add(m);
        }
        return list;
    }

    public void linkItem(String materialKey, String modelId) {
        itemToModel.put(materialKey, modelId);
        save();
    }

    public String getModelForItem(String materialKey) {
        return itemToModel.get(materialKey);
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection modelsSection = yml.getConfigurationSection("models");
        if (modelsSection != null) {
            for (String id : modelsSection.getKeys(false)) {
                ConfigurationSection s = modelsSection.getConfigurationSection(id);
                TowerModel model = new TowerModel(id);
                model.setDisplayName(s.getString("display-name", id));
                model.setSpaceCost(s.getInt("space"));
                model.setPrice(s.getInt("price"));
                model.setDamage(s.getDouble("damage"));
                model.setSpeed(TowerModel.Speed.fromString(s.getString("speed", "MEDIUM")));
                model.setWidth(s.getInt("width"));
                model.setHeight(s.getInt("height"));
                model.setRange(s.getDouble("range"));
                model.setZOffset(s.getDouble("z-offset"));
                model.setUpgradesFrom(s.getString("upgrades-from", null));
                model.setLinkedItemMaterial(s.getString("linked-item", null));
                for (String line : s.getStringList("blocks")) {
                    model.getBlocks().add(RelativeBlock.deserialize(line));
                }
                models.put(id, model);
            }
        }

        shopModelIds.addAll(yml.getStringList("shop"));

        ConfigurationSection itemLinks = yml.getConfigurationSection("item-links");
        if (itemLinks != null) {
            for (String key : itemLinks.getKeys(false)) {
                itemToModel.put(key, itemLinks.getString(key));
            }
        }
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        for (TowerModel m : models.values()) {
            String base = "models." + m.getModelId();
            yml.set(base + ".display-name", m.getDisplayName());
            yml.set(base + ".space", m.getSpaceCost());
            yml.set(base + ".price", m.getPrice());
            yml.set(base + ".damage", m.getDamage());
            yml.set(base + ".speed", m.getSpeed().name());
            yml.set(base + ".width", m.getWidth());
            yml.set(base + ".height", m.getHeight());
            yml.set(base + ".range", m.getRange());
            yml.set(base + ".z-offset", m.getZOffset());
            yml.set(base + ".upgrades-from", m.getUpgradesFrom());
            yml.set(base + ".linked-item", m.getLinkedItemMaterial());
            List<String> blockLines = new ArrayList<>();
            for (RelativeBlock rb : m.getBlocks()) blockLines.add(rb.serialize());
            yml.set(base + ".blocks", blockLines);
        }
        yml.set("shop", new ArrayList<>(shopModelIds));
        for (Map.Entry<String, String> e : itemToModel.entrySet()) {
            yml.set("item-links." + e.getKey(), e.getValue());
        }

        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save towers.yml: " + e.getMessage());
        }
    }
}
