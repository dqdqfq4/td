package com.tdtycoon.plugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a tower TYPE (e.g. "copper-1"), analogous to what
 * /create_tower registers. Placed instances reference this by modelId.
 */
public class TowerModel {

    public enum Speed {
        SLOW(40), MEDIUM(25), FAST(12); // ticks between attacks

        public final int cooldownTicks;
        Speed(int cooldownTicks) { this.cooldownTicks = cooldownTicks; }

        public static Speed fromString(String s) {
            try {
                return Speed.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MEDIUM;
            }
        }
    }

    private final String modelId;
    private String displayName;
    private int spaceCost;
    private int price;
    private double damage;
    private Speed speed;
    private int width;
    private int height;
    private double range;
    private double zOffset;

    private String upgradesFrom; // modelId or null
    private String linkedItemMaterial; // material used as the placement item icon
    private final List<RelativeBlock> blocks = new ArrayList<>();

    public TowerModel(String modelId) {
        this.modelId = modelId;
        this.displayName = modelId;
    }

    public String getModelId() { return modelId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getSpaceCost() { return spaceCost; }
    public void setSpaceCost(int spaceCost) { this.spaceCost = spaceCost; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    public Speed getSpeed() { return speed; }
    public void setSpeed(Speed speed) { this.speed = speed; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }

    public double getZOffset() { return zOffset; }
    public void setZOffset(double zOffset) { this.zOffset = zOffset; }

    public String getUpgradesFrom() { return upgradesFrom; }
    public void setUpgradesFrom(String upgradesFrom) { this.upgradesFrom = upgradesFrom; }

    public String getLinkedItemMaterial() { return linkedItemMaterial; }
    public void setLinkedItemMaterial(String linkedItemMaterial) { this.linkedItemMaterial = linkedItemMaterial; }

    public List<RelativeBlock> getBlocks() { return blocks; }
}
