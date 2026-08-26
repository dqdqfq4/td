package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.RelativeBlock;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles /copywand (region-select tool) and /copydisplay (bakes the
 * selection into a TowerModel's block list, relative to a computed origin).
 */
public class BuildCaptureManager {

    private final TowerDefensePlugin plugin;
    private final NamespacedKey wandKey;
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public BuildCaptureManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "copywand");
    }

    public ItemStack createWand() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bCopy Wand");
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    public void setPos1(UUID player, Location loc) { pos1.put(player, loc); }
    public void setPos2(UUID player, Location loc) { pos2.put(player, loc); }

    public boolean hasSelection(UUID player) {
        return pos1.containsKey(player) && pos2.containsKey(player);
    }

    /**
     * Captures the selected region into (or updates) the named TowerModel's
     * block list. Origin is the bottom-center of the selection, shifted by
     * zOffset along the Z axis, matching the video's "/copydisplay z-offset model-name".
     */
    public TowerModel captureToModel(TowerModelManager modelManager, UUID player, double zOffset, String modelName) {
        Location a = pos1.get(player);
        Location b = pos2.get(player);
        if (a == null || b == null) return null;

        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int maxX = Math.max(a.getBlockX(), b.getBlockX());
        int minY = Math.min(a.getBlockY(), b.getBlockY());
        int maxY = Math.max(a.getBlockY(), b.getBlockY());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());

        double originX = (minX + maxX) / 2.0 + 0.5;
        double originY = minY;
        double originZ = (minZ + maxZ) / 2.0 + 0.5 + zOffset;

        TowerModel model = modelManager.getModel(modelName);
        if (model == null) model = new TowerModel(modelName);
        model.getBlocks().clear();
        model.setZOffset(zOffset);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var block = a.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) continue;
                    double relX = (x + 0.5) - originX;
                    double relY = y - originY;
                    double relZ = (z + 0.5) - originZ;
                    model.getBlocks().add(new RelativeBlock(relX, relY, relZ, block.getBlockData().getAsString()));
                }
            }
        }

        modelManager.registerModel(model);
        return model;
    }
}
