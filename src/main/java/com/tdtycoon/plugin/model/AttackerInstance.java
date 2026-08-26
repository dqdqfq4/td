package com.tdtycoon.plugin.model;

import java.util.UUID;

/** Tracks one spawned attacker mob's progress along its plot's path. */
public class AttackerInstance {

    private final UUID entityId;
    private final UUID owner;
    private int waypointIndex = 0;

    public AttackerInstance(UUID entityId, UUID owner) {
        this.entityId = entityId;
        this.owner = owner;
    }

    public UUID getEntityId() { return entityId; }
    public UUID getOwner() { return owner; }

    public int getWaypointIndex() { return waypointIndex; }
    public void setWaypointIndex(int waypointIndex) { this.waypointIndex = waypointIndex; }
}
