package com.tdtycoon.plugin.listener;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.gui.CampRepairGUI;
import com.tdtycoon.plugin.gui.ShopGUI;
import com.tdtycoon.plugin.gui.TowerPanelGUI;
import com.tdtycoon.plugin.gui.TowerUpgradeGUI;
import com.tdtycoon.plugin.model.PlacedTower;
import com.tdtycoon.plugin.model.PlayerData;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {

    private final TowerDefensePlugin plugin;

    public GUIListener(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (title.equals(ShopGUI.TITLE)) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;
            String modelId = clicked.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getShopGUI().getModelKey(), PersistentDataType.STRING);
            if (modelId == null) return;
            TowerModel model = plugin.getTowerModelManager().getModel(modelId);
            if (model != null) plugin.getShopGUI().purchase(player, model);
            return;
        }

        if (title.equals(CampRepairGUI.TITLE)) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;
            Byte flag = clicked.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getCampRepairGUI().getRepairKey(), PersistentDataType.BYTE);
            if (flag != null) plugin.getCampRepairGUI().handleClick(player, event.getInventory());
            return;
        }

        if (title.equals(TowerPanelGUI.TITLE)) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;
            String action = clicked.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getTowerPanelGUI().getActionKey(), PersistentDataType.STRING);
            handlePanelAction(player, action);
            return;
        }

        if (title.endsWith(" Panel") && !title.equals(TowerPanelGUI.TITLE)) {
            // per-tower upgrade panel
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;
            String action = clicked.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getTowerUpgradeGUI().getActionKey(), PersistentDataType.STRING);
            if ("upgrade".equals(action)) {
                PlacedTower tower = plugin.getTowerUpgradeGUI().getContext(player);
                if (tower != null) plugin.getTowerUpgradeGUI().handleUpgradeClick(player, tower);
            }
        }
    }

    private void handlePanelAction(Player player, String action) {
        if (action == null) return;
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        switch (action) {
            case "shop" -> {
                player.closeInventory();
                plugin.getShopGUI().open(player);
            }
            case "toggle-pause" -> {
                if (data.isAttacksPaused()) {
                    plugin.getWaveManager().startWave(player.getUniqueId());
                } else {
                    data.setAttacksPaused(true);
                }
                player.closeInventory();
            }
            case "visit" -> {
                player.sendMessage("§eVisit-others isn't wired up yet in this build.");
                player.closeInventory();
            }
            case "prestige" -> {
                player.sendMessage("§ePrestige isn't wired up yet in this build.");
                player.closeInventory();
            }
        }
    }
}
