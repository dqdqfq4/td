package com.tdtycoon.plugin.model;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private long coins;
    private int spaceUsed;
    private int maxSpace;
    private int prestige;
    private double campHealth;   // current, 0..maxCampHealth
    private double maxCampHealth;
    private boolean attacksPaused;
    private int plotIndex = -1; // assigned by PlotManager, -1 = unassigned

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.coins = 0;
        this.spaceUsed = 0;
        this.maxSpace = 20;
        this.prestige = 1;
        this.maxCampHealth = 100;
        this.campHealth = 100;
        this.attacksPaused = true;
    }

    public UUID getUuid() { return uuid; }

    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = Math.max(0, coins); }
    public void addCoins(long amount) { this.coins += amount; }
    public boolean spendCoins(long amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    public int getSpaceUsed() { return spaceUsed; }
    public void setSpaceUsed(int spaceUsed) { this.spaceUsed = spaceUsed; }

    public int getMaxSpace() { return maxSpace; }
    public void setMaxSpace(int maxSpace) { this.maxSpace = maxSpace; }

    public boolean hasSpaceFor(int cost) { return spaceUsed + cost <= maxSpace; }

    public int getPrestige() { return prestige; }
    public void setPrestige(int prestige) { this.prestige = prestige; }

    public double getCampHealth() { return campHealth; }
    public void setCampHealth(double campHealth) {
        this.campHealth = Math.max(0, Math.min(maxCampHealth, campHealth));
    }

    public double getMaxCampHealth() { return maxCampHealth; }
    public void setMaxCampHealth(double maxCampHealth) { this.maxCampHealth = maxCampHealth; }

    public double getCampHealthPercent() {
        if (maxCampHealth <= 0) return 0;
        return (campHealth / maxCampHealth) * 100.0;
    }

    public boolean isAttacksPaused() { return attacksPaused; }
    public void setAttacksPaused(boolean attacksPaused) { this.attacksPaused = attacksPaused; }

    public int getPlotIndex() { return plotIndex; }
    public void setPlotIndex(int plotIndex) { this.plotIndex = plotIndex; }
}
