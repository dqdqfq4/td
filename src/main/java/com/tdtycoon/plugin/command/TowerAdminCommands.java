package com.tdtycoon.plugin.command;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TowerAdminCommands implements CommandExecutor {

    private final TowerDefensePlugin plugin;

    public TowerAdminCommands(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("towerdefense.admin")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "create_tower" -> createTower(player, args);
            case "rename" -> rename(player, args);
            case "settoweritem" -> setTowerItem(player, args);
            case "shopadd" -> shopAdd(player);
            case "shopremove" -> shopRemove(player, args);
            case "settowerupgrade" -> setTowerUpgrade(player, args);
            default -> { return false; }
        }
        return true;
    }

    // /create_tower <model-name> <space> <price> <damage> <speed> <width> <height> <range>
    private void createTower(Player player, String[] args) {
        if (args.length < 8) {
            player.sendMessage("§cUsage: /create_tower <model-name> <space> <price> <damage> <speed> <width> <height> <range>");
            return;
        }
        String modelName = args[0];
        try {
            int space = Integer.parseInt(args[1]);
            int price = Integer.parseInt(args[2]);
            double damage = Double.parseDouble(args[3]);
            TowerModel.Speed speed = TowerModel.Speed.fromString(args[4]);
            int width = Integer.parseInt(args[5]);
            int height = Integer.parseInt(args[6]);
            double range = Double.parseDouble(args[7]);

            TowerModel model = plugin.getTowerModelManager().getModel(modelName);
            if (model == null) {
                player.sendMessage("§eNo captured visual found for '" + modelName +
                        "' yet — run /copydisplay first if you want a custom model. Registering stats-only for now.");
                model = new TowerModel(modelName);
            }
            model.setSpaceCost(space);
            model.setPrice(price);
            model.setDamage(damage);
            model.setSpeed(speed);
            model.setWidth(width);
            model.setHeight(height);
            model.setRange(range);

            plugin.getTowerModelManager().registerModel(model);
            player.sendMessage("§aTower '" + modelName + "' created.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cOne or more numeric arguments were invalid.");
        }
    }

    private void rename(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /rename <text...>");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cHold an item to rename.");
            return;
        }
        String name = String.join(" ", args);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        player.sendMessage("§aRenamed item.");
    }

    private void setTowerItem(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /settoweritem <model-name>");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cHold an item to link.");
            return;
        }
        String modelId = args[0];
        TowerModel model = plugin.getTowerModelManager().getModel(modelId);
        if (model == null) {
            player.sendMessage("§cNo tower model named '" + modelId + "' exists yet.");
            return;
        }
        model.setLinkedItemMaterial(item.getType().name());
        plugin.getTowerModelManager().linkItem(item.getType().name(), modelId);
        plugin.getTowerModelManager().registerModel(model);
        player.sendMessage("§aLinked " + item.getType() + " to model '" + modelId + "'.");
    }

    private void shopAdd(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cHold the tower item you want to add.");
            return;
        }
        String modelId = plugin.getTowerModelManager().getModelForItem(item.getType().name());
        if (modelId == null) {
            player.sendMessage("§cThis item isn't linked to any tower model. Use /settoweritem first.");
            return;
        }
        plugin.getTowerModelManager().addToShop(modelId);
        player.sendMessage("§aAdded '" + modelId + "' to the shop.");
    }

    private void shopRemove(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /shopremove <model-name>");
            return;
        }
        plugin.getTowerModelManager().removeFromShop(args[0]);
        player.sendMessage("§aRemoved '" + args[0] + "' from the shop.");
    }

    private void setTowerUpgrade(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /settowerupgrade <base-model> <upgrade-model>");
            return;
        }
        TowerModel base = plugin.getTowerModelManager().getModel(args[0]);
        TowerModel upgrade = plugin.getTowerModelManager().getModel(args[1]);
        if (base == null || upgrade == null) {
            player.sendMessage("§cBoth models must already exist.");
            return;
        }
        upgrade.setUpgradesFrom(base.getModelId());
        plugin.getTowerModelManager().registerModel(upgrade);
        player.sendMessage("§a" + args[1] + " is now an upgrade of " + args[0] + ".");
    }
}
