package com.tdtycoon.plugin.command;

import com.tdtycoon.plugin.TowerDefensePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommands implements CommandExecutor {

    private final TowerDefensePlugin plugin;

    public BuildCommands(TowerDefensePlugin plugin) {
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

        if (label.equalsIgnoreCase("copywand")) {
            player.getInventory().addItem(plugin.getBuildCaptureManager().createWand());
            player.sendMessage("§bLeft-click for position 1, right-click for position 2.");
            return true;
        }

        if (label.equalsIgnoreCase("copydisplay")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /copydisplay <zOffset> <model-name>");
                return true;
            }
            if (!plugin.getBuildCaptureManager().hasSelection(player.getUniqueId())) {
                player.sendMessage("§cSelect two positions with the copy wand first.");
                return true;
            }
            double zOffset;
            try {
                zOffset = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cz-offset must be a number.");
                return true;
            }
            String modelName = args[1];
            var model = plugin.getBuildCaptureManager().captureToModel(
                    plugin.getTowerModelManager(), player.getUniqueId(), zOffset, modelName);
            player.sendMessage("§aCaptured " + model.getBlocks().size() + " blocks into model '" + modelName + "'.");
            return true;
        }

        return false;
    }
}
