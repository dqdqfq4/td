package com.tdtycoon.plugin;

import com.tdtycoon.plugin.command.BuildCommands;
import com.tdtycoon.plugin.command.PlayerCommands;
import com.tdtycoon.plugin.command.TowerAdminCommands;
import com.tdtycoon.plugin.gui.CampRepairGUI;
import com.tdtycoon.plugin.gui.ShopGUI;
import com.tdtycoon.plugin.gui.TowerPanelGUI;
import com.tdtycoon.plugin.gui.TowerUpgradeGUI;
import com.tdtycoon.plugin.listener.GUIListener;
import com.tdtycoon.plugin.listener.PlayerInteractListener;
import com.tdtycoon.plugin.listener.PlayerJoinListener;
import com.tdtycoon.plugin.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public class TowerDefensePlugin extends JavaPlugin {

    private static TowerDefensePlugin instance;

    private PlotManager plotManager;
    private PlotVisualManager plotVisualManager;
    private TowerModelManager towerModelManager;
    private PlayerDataManager playerDataManager;
    private BuildCaptureManager buildCaptureManager;
    private TowerManager towerManager;
    private WaveManager waveManager;
    private CampManager campManager;
    private ScoreboardService scoreboardService;

    private ShopGUI shopGUI;
    private TowerUpgradeGUI towerUpgradeGUI;
    private CampRepairGUI campRepairGUI;
    private TowerPanelGUI towerPanelGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // managers (order matters: some depend on config/others being ready)
        towerModelManager = new TowerModelManager(this);
        playerDataManager = new PlayerDataManager(this);
        buildCaptureManager = new BuildCaptureManager(this);
        plotManager = new PlotManager(this);
        plotVisualManager = new PlotVisualManager(this);
        towerManager = new TowerManager(this);
        campManager = new CampManager(this);
        waveManager = new WaveManager(this);
        scoreboardService = new ScoreboardService(this);

        // guis
        shopGUI = new ShopGUI(this);
        towerUpgradeGUI = new TowerUpgradeGUI(this);
        campRepairGUI = new CampRepairGUI(this);
        towerPanelGUI = new TowerPanelGUI(this);

        // listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // commands
        getCommand("copywand").setExecutor(new BuildCommands(this));
        getCommand("copydisplay").setExecutor(new BuildCommands(this));
        getCommand("create_tower").setExecutor(new TowerAdminCommands(this));
        getCommand("rename").setExecutor(new TowerAdminCommands(this));
        getCommand("settoweritem").setExecutor(new TowerAdminCommands(this));
        getCommand("shopadd").setExecutor(new TowerAdminCommands(this));
        getCommand("shopremove").setExecutor(new TowerAdminCommands(this));
        getCommand("settowerupgrade").setExecutor(new TowerAdminCommands(this));
        getCommand("shop").setExecutor(new PlayerCommands(this));
        getCommand("camppanel").setExecutor(new PlayerCommands(this));
        getCommand("towerpanel").setExecutor(new PlayerCommands(this));
        getCommand("prestige").setExecutor(new PlayerCommands(this));
        getCommand("givecoins").setExecutor(new PlayerCommands(this));

        waveManager.start();
        // refresh every player's HUD once a second
        getServer().getScheduler().runTaskTimer(this, scoreboardService::updateAll, 20L, 20L);
        // autosave every 5 minutes
        getServer().getScheduler().runTaskTimer(this, playerDataManager::saveAll, 6000L, 6000L);

        // Pre-generate plots on startup
        new PlotPreGenerator(this).preGeneratePlots();

        getLogger().info("Tower Defense Tycoon enabled.");
    }

    @Override
    public void onDisable() {
        if (waveManager != null) waveManager.stop();
        if (plotVisualManager != null) plotVisualManager.shutdown();
        if (playerDataManager != null) playerDataManager.saveAll();
        getLogger().info("Tower Defense Tycoon disabled.");
    }

    public static TowerDefensePlugin get() { return instance; }

    public PlotManager getPlotManager() { return plotManager; }
    public PlotVisualManager getPlotVisualManager() { return plotVisualManager; }
    public TowerModelManager getTowerModelManager() { return towerModelManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public BuildCaptureManager getBuildCaptureManager() { return buildCaptureManager; }
    public TowerManager getTowerManager() { return towerManager; }
    public WaveManager getWaveManager() { return waveManager; }
    public CampManager getCampManager() { return campManager; }
    public ScoreboardService getScoreboardService() { return scoreboardService; }

    public ShopGUI getShopGUI() { return shopGUI; }
    public TowerUpgradeGUI getTowerUpgradeGUI() { return towerUpgradeGUI; }
    public CampRepairGUI getCampRepairGUI() { return campRepairGUI; }
    public TowerPanelGUI getTowerPanelGUI() { return towerPanelGUI; }
}
