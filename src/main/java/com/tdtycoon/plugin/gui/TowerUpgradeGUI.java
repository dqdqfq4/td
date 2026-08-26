package com.tdtycoon.plugin.gui;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlacedTower;
import com.tdtycoon.plugin.model.PlayerData;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TowerUpgradeGUI {

    private final TowerDefensePlugin plugin;
    private final NamespacedKey actionKey;
    // maps an open inventory to the tower it belongs to, so clicks resolve back to it
    private final Map<UUID, PlacedTower> openContext = new HashMap<>();

    public TowerUpgradeGUI(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "upgrade-action");
    }

    public NamespacedKey getActionKey() { return actionKey; }
    public PlacedTower getContext(Player player) { return openContext.get(player.getUniqueId()); }
    public void clearContext(Player player) { openContext.remove(player.getUniqueId()); }

    public void open(Player player, PlacedTower tower) {
        TowerModel model = plugin.getTowerModelManager().getModel(tower.getModelId());
        if (model == null) return;

        Inventory inv = plugin.getServer().createInventory(null, 27, model.getDisplayName() + " Panel");

        // find whichever registered model has this one as its upgrades-from
        TowerModel nextModel = null;
        for (TowerModel candidate : plugin.getTowerModelManager().getAllModels()) {
            if (model.getModelId().equals(candidate.getUpgradesFrom())) {
                nextModel = candidate;
                break;
            }
        }

        if (nextModel != null) {
            ItemStack upgradeItem = new ItemStack(Material.EMERALD);
            ItemMeta meta = upgradeItem.getItemMeta();
            meta.setDisplayName("§aUpgrade Tower");
            List<String> lore = new ArrayList<>();
            lore.add("§eUpgrade for " + nextModel.getPrice() + " coins");
            lore.add("§7-> " + nextModel.getDisplayName());
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "upgrade");
            upgradeItem.setItemMeta(meta);
            inv.setItem(13, upgradeItem);
        } else {
            ItemStack maxed = new ItemStack(Material.BARRIER);
            ItemMeta meta = maxed.getItemMeta();
            meta.setDisplayName("§cMax Level");
            maxed.setItemMeta(meta);
            inv.setItem(13, maxed);
        }

        openContext.put(player.getUniqueId(), tower);
        player.openInventory(inv);
    }

    public void handleUpgradeClick(Player player, PlacedTower tower) {
        TowerModel current = plugin.getTowerModelManager().getModel(tower.getModelId());
        TowerModel next = null;
        for (TowerModel candidate : plugin.getTowerModelManager().getAllModels()) {
            if (current.getModelId().equals(candidate.getUpgradesFrom())) {
                next = candidate;
                break;
            }
        }
        if (next == null) return;

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int spaceDelta = next.getSpaceCost() - current.getSpaceCost();
        if (!data.spendCoins(next.getPrice())) {
            player.sendMessage("§cNot enough coins.");
            return;
        }
        if (spaceDelta > 0 && !data.hasSpaceFor(spaceDelta)) {
            data.addCoins(next.getPrice());
            player.sendMessage("§cNot enough space.");
            return;
        }
        data.setSpaceUsed(data.getSpaceUsed() + spaceDelta);
        plugin.getTowerManager().upgradeTower(tower, next);
        player.sendMessage("§aUpgraded to " + next.getDisplayName() + "!");
        player.closeInventory();
    }
}
