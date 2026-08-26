package com.tdtycoon.plugin.model;

import org.bukkit.Location;
import org.bukkit.entity.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A tower a player has actually placed in their plot.
 */
public class PlacedTower {

    private final UUID id = UUID.randomUUID();
    private final UUID owner;
    private String modelId;
    private Location location; // absolute world location (base of the tower)
    private long lastAttackTick = 0;

    // Entity handles so we can clean them up on upgrade/removal
    private final List<UUID> displayEntityIds = new ArrayList<>();

    public PlacedTower(UUID owner, String modelId, Location location) {
        this.owner = owner;
        this.modelId = modelId;
        this.location = location;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public Location getLocation() { return location; }

    public long getLastAttackTick() { return lastAttackTick; }
    public void setLastAttackTick(long lastAttackTick) { this.lastAttackTick = lastAttackTick; }

    public List<UUID> getDisplayEntityIds() { return displayEntityIds; }
}
