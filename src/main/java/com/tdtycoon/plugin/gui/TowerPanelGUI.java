package com.tdtycoon.plugin.gui;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class TowerPanelGUI {

    public static final String TITLE = "Tower Panel";
    private final TowerDefensePlugin plugin;
    private final NamespacedKey actionKey;

    public TowerPanelGUI(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "panel-action");
    }

    public NamespacedKey getActionKey() { return actionKey; }

    public void open(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 9, TITLE);
        inv.setItem(1, icon(Material.CHEST, "§eOpen Shop", "shop"));

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        boolean paused = data.isAttacksPaused();
        inv.setItem(3, icon(paused ? Material.LIME_DYE : Material.RED_DYE,
                paused ? "§aStart Wave" : "§cPause Attacks", "toggle-pause"));

        inv.setItem(5, icon(Material.ENDER_PEARL, "§bVisit Others", "visit"));
        inv.setItem(7, icon(Material.NETHER_STAR, "§dPrestige", "prestige"));
        player.openInventory(inv);
    }

    private ItemStack icon(Material mat, String name, String action) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        return item;
    }
}
