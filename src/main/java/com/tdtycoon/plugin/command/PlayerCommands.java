package com.tdtycoon.plugin.command;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommands implements CommandExecutor {

    private final TowerDefensePlugin plugin;

    public PlayerCommands(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "shop" -> plugin.getShopGUI().open(player);
            case "camppanel" -> plugin.getCampRepairGUI().open(player);
            case "towerpanel" -> plugin.getTowerPanelGUI().open(player);
            case "prestige" -> doPrestige(player);
            default -> { return false; }
        }
        return true;
    }

    private void doPrestige(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.getCoins() < 50000) {
            player.sendMessage("§cYou need at least 50,000 coins to prestige.");
            return;
        }
        data.setCoins(0);
        data.setSpaceUsed(0);
        data.setMaxSpace(data.getMaxSpace() + 5);
        data.setPrestige(data.getPrestige() + 1);
        player.sendMessage("§dPrestiged! You are now prestige " + data.getPrestige() + ".");
    }
}
