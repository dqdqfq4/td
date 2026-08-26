package com.tdtycoon.plugin.gui;

import com.tdtycoon.plugin.TowerDefensePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class CampRepairGUI {

    public static final String TITLE = "Camp Panel";
    private final TowerDefensePlugin plugin;
    private final NamespacedKey repairKey;

    public CampRepairGUI(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.repairKey = new NamespacedKey(plugin, "camp-repair-button");
    }

    public NamespacedKey getRepairKey() { return repairKey; }

    public void open(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 9, TITLE);
        inv.setItem(4, buildRepairIcon(player));
        player.openInventory(inv);
    }

    private ItemStack buildRepairIcon(Player player) {
        ItemStack item = new ItemStack(Material.TORCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Repair your camp");
        int clicksNeeded = plugin.getConfig().getInt("economy.camp-repair-clicks-per-point", 2);
        double progress = plugin.getCampManager().getRepairProgressPercent(player.getUniqueId());

        List<String> lore = new ArrayList<>();
        lore.add("§7For every " + clicksNeeded + " clicks: §a+1 HEALTH");
        lore.add("§7Progress: " + progressBar(progress) + " §f(" + Math.round(progress) + "%)");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(repairKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private String progressBar(double percent) {
        int bars = 10;
        int filled = (int) Math.round((percent / 100.0) * bars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars; i++) sb.append(i < filled ? "§a|" : "§7|");
        return sb.toString();
    }

    /** Registers a click and refreshes the icon in place so progress is visible live. */
    public void handleClick(Player player, Inventory inv) {
        boolean gained = plugin.getCampManager().registerRepairClick(player.getUniqueId());
        inv.setItem(4, buildRepairIcon(player));
        if (gained) {
            player.sendMessage("§a+1 camp health!");
        }
    }
}
