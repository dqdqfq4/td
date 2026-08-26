package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardService {

    private final TowerDefensePlugin plugin;

    public ScoreboardService(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        ScoreboardManager manager = plugin.getServer().getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("td", "dummy", "§b§lTOWER DEFENSE");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 5;
        obj.getScore("§7").setScore(line--); // spacer
        obj.getScore("§fCOINS: §6" + data.getCoins()).setScore(line--);
        obj.getScore("§fSPACE: §d" + data.getSpaceUsed() + "/" + data.getMaxSpace()).setScore(line--);
        obj.getScore("§fPRESTIGE: §a" + data.getPrestige()).setScore(line--);
        obj.getScore("§8 ").setScore(line--); // spacer
        obj.getScore("§7play.yourserver.net").setScore(line);

        player.setScoreboard(board);
    }

    /** Call periodically (e.g. every second) so coin/space changes reflect without a rejoin. */
    public void updateAll() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            update(p);
        }
    }
}
