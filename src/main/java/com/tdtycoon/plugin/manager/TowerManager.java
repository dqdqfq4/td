package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.PlacedTower;
import com.tdtycoon.plugin.model.RelativeBlock;
import com.tdtycoon.plugin.model.TowerModel;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Owns every PlacedTower in the world: spawning their BlockDisplay visuals,
 * finding targets in range, dealing damage, and firing the attack particle.
 */
public class TowerManager {

    private final TowerDefensePlugin plugin;
    private final Map<UUID, List<PlacedTower>> towersByOwner = new HashMap<>();

    public TowerManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
    }

    public PlacedTower placeTower(UUID owner, TowerModel model, Location baseLocation) {
        PlacedTower tower = new PlacedTower(owner, model.getModelId(), baseLocation);
        spawnDisplays(tower, model);
        towersByOwner.computeIfAbsent(owner, k -> new ArrayList<>()).add(tower);
        return tower;
    }

    public void upgradeTower(PlacedTower tower, TowerModel newModel) {
        removeDisplays(tower);
        tower.setModelId(newModel.getModelId());
        spawnDisplays(tower, newModel);
    }

    public void removeTower(PlacedTower tower) {
        removeDisplays(tower);
        List<PlacedTower> list = towersByOwner.get(tower.getOwner());
        if (list != null) list.remove(tower);
    }

    private void spawnDisplays(PlacedTower tower, TowerModel model) {
        Location base = tower.getLocation();
        for (RelativeBlock rb : model.getBlocks()) {
            Location spawnAt = base.clone().add(rb.getOffsetX(), rb.getOffsetY(), rb.getOffsetZ());
            BlockDisplay display = base.getWorld().spawn(spawnAt, BlockDisplay.class);
            BlockData data = plugin.getServer().createBlockData(rb.getBlockDataString());
            display.setBlock(data);
            // center the display on its origin block like a normal block-space entity
            Transformation t = display.getTransformation();
            t.getTranslation().set(-0.5f, 0f, -0.5f);
            display.setTransformation(t);
            tower.getDisplayEntityIds().add(display.getUniqueId());
        }
    }

    private void removeDisplays(PlacedTower tower) {
        for (UUID id : tower.getDisplayEntityIds()) {
            Entity e = plugin.getServer().getEntity(id);
            if (e != null) e.remove();
        }
        tower.getDisplayEntityIds().clear();
    }

    public List<PlacedTower> getTowers(UUID owner) {
        return towersByOwner.getOrDefault(owner, Collections.emptyList());
    }

    public List<PlacedTower> getAllTowers() {
        List<PlacedTower> all = new ArrayList<>();
        for (List<PlacedTower> list : towersByOwner.values()) all.addAll(list);
        return all;
    }

    /**
     * One combat tick for a single owner's towers against their active attackers.
     * Called every server tick from WaveManager's loop, per plot.
     */
    public void tickCombat(UUID owner, long currentTick, List<LivingEntity> attackers) {
        if (attackers.isEmpty()) return;
        for (PlacedTower tower : getTowers(owner)) {
            TowerModel model = plugin.getTowerModelManager().getModel(tower.getModelId());
            if (model == null) continue;
            if (currentTick - tower.getLastAttackTick() < model.getSpeed().cooldownTicks) continue;

            LivingEntity target = findClosestInRange(tower.getLocation(), model.getRange(), attackers);
            if (target == null) continue;

            tower.setLastAttackTick(currentTick);
            fireAbility(tower, model, target);
        }
    }

    private LivingEntity findClosestInRange(Location towerLoc, double range, List<LivingEntity> candidates) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : candidates) {
            if (e.isDead() || !e.getWorld().equals(towerLoc.getWorld())) continue;
            double d = e.getLocation().distanceSquared(towerLoc);
            if (d <= range * range && d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    /**
     * Equivalent of the Skript "playAbility" call in the video's towerAbility
     * function: spawns a colored particle trail from tower to target and
     * applies damage. Per-model color/particle can be layered on later.
     */
    private void fireAbility(PlacedTower tower, TowerModel model, LivingEntity target) {
        Location from = tower.getLocation().clone().add(0, model.getHeight() / 2.0, 0);
        Location to = target.getLocation().clone().add(0, 1, 0);

        Vector3f dir = new Vector3f((float) (to.getX() - from.getX()),
                (float) (to.getY() - from.getY()), (float) (to.getZ() - from.getZ()));
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Location point = from.clone().add(dir.x * t, dir.y * t, dir.z * t);
            from.getWorld().spawnParticle(Particle.CRIT, point, 1, 0, 0, 0, 0);
        }

        target.damage(model.getDamage());
    }
}
