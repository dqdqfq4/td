package com.tdtycoon.plugin.gui;

import com.tdtycoon.plugin.TowerDefensePlugin;
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
import java.util.List;

public class ShopGUI {

    public static final String TITLE = "Towers Shop";
    private final TowerDefensePlugin plugin;
    private final NamespacedKey modelKey;

    public ShopGUI(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        this.modelKey = new NamespacedKey(plugin, "shop-model-id");
    }

    public NamespacedKey getModelKey() { return modelKey; }

    public void open(Player player) {
        List<TowerModel> shopModels = plugin.getTowerModelManager().getShopModels();
        int rows = Math.max(1, (int) Math.ceil(shopModels.size() / 9.0));
        Inventory inv = plugin.getServer().createInventory(null, rows * 9, TITLE);

        for (TowerModel model : shopModels) {
            inv.addItem(buildIcon(model));
        }
        player.openInventory(inv);
    }

    private ItemStack buildIcon(TowerModel model) {
        Material icon = Material.STONE_BUTTON;
        try {
            if (model.getLinkedItemMaterial() != null) {
                icon = Material.valueOf(model.getLinkedItemMaterial());
            }
        } catch (IllegalArgumentException ignored) {}

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + model.getDisplayName());

        List<String> lore = new ArrayList<>();
        lore.add("§7DAMAGE: §c" + (int) model.getDamage());
        lore.add("§7SPEED: §b" + model.getSpeed().name());
        lore.add("§7RANGE: §a+" + model.getRange());
        lore.add("§7SPACE: §d+" + model.getSpaceCost());
        lore.add("");
        lore.add("§6PURCHASE FOR " + model.getPrice() + " COINS");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(modelKey, PersistentDataType.STRING, model.getModelId());
        item.setItemMeta(meta);
        return item;
    }

    /** Attempts to purchase the given model for the player: charges coins, gives the placement item. */
    public boolean purchase(Player player, TowerModel model) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (!data.spendCoins(model.getPrice())) {
            player.sendMessage("§cNot enough coins.");
            return false;
        }
        if (!data.hasSpaceFor(model.getSpaceCost())) {
            player.sendMessage("§cNot enough space.");
            data.addCoins(model.getPrice()); // refund
            return false;
        }
        ItemStack placementItem = buildIcon(model);
        placementItem.setAmount(1);
        player.getInventory().addItem(placementItem);
        player.sendMessage("§aPurchased " + model.getDisplayName() + "!");
        return true;
    }
}
