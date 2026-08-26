package com.tdtycoon.plugin.listener;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlacedTower;
import com.tdtycoon.plugin.model.PlayerData;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractListener implements Listener {

    private final TowerDefensePlugin plugin;

    public PlayerInteractListener(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = event.getItem();

        // --- copywand selection ---
        if (hand != null && plugin.getBuildCaptureManager().isWand(hand) && event.getClickedBlock() != null) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                plugin.getBuildCaptureManager().setPos1(player.getUniqueId(), event.getClickedBlock().getLocation());
                player.sendMessage("§bPosition 1 set.");
                event.setCancelled(true);
                return;
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                plugin.getBuildCaptureManager().setPos2(player.getUniqueId(), event.getClickedBlock().getLocation());
                player.sendMessage("§bPosition 2 set.");
                event.setCancelled(true);
                return;
            }
        }

        // --- placing a purchased tower item ---
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && hand != null && hand.hasItemMeta()) {
            String modelId = hand.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getShopGUI().getModelKey(), PersistentDataType.STRING);
            if (modelId != null) {
                event.setCancelled(true);
                placeTower(player, modelId, event.getClickedBlock().getLocation().add(0.5, 1, 0.5));
                return;
            }
        }

        // --- right-clicking a placed tower's base opens its upgrade panel ---
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (hand == null || hand.getType().isAir())) {
            var clickedLoc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
            for (PlacedTower tower : plugin.getTowerManager().getTowers(player.getUniqueId())) {
                if (tower.getLocation().distanceSquared(clickedLoc) < 1.0) {
                    plugin.getTowerUpgradeGUI().open(player, tower);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void placeTower(Player player, String modelId, org.bukkit.Location location) {
        TowerModel model = plugin.getTowerModelManager().getModel(modelId);
        if (model == null) return;

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (!data.hasSpaceFor(model.getSpaceCost())) {
            player.sendMessage("§cNot enough space to place this tower.");
            return;
        }

        plugin.getTowerManager().placeTower(player.getUniqueId(), model, location);
        data.setSpaceUsed(data.getSpaceUsed() + model.getSpaceCost());

        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);
        player.sendMessage("§aPlaced " + model.getDisplayName() + "!");
    }
}
