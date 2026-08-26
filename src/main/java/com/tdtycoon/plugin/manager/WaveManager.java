package com.tdtycoon.plugin.manager;

import com.tdtycoon.plugin.TowerDefensePlugin;
import com.tdtycoon.plugin.model.AttackerInstance;
import com.tdtycoon.plugin.model.PlayerData;
import com.tdtycoon.plugin.model.Plot;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Spawns waves of attackers per plot and walks them along the configured
 * path waypoints toward the camp, tick by tick. Also feeds the current
 * attacker list to TowerManager each tick so towers have something to shoot.
 */
public class WaveManager {

    private final TowerDefensePlugin plugin;
    private final Map<UUID, List<AttackerInstance>> activeByOwner = new HashMap<>();
    private final Map<UUID, Integer> spawnedThisWave = new HashMap<>();
    // entities removed for reaching the camp, so cleanupDead doesn't mistake them for kills
    private final Set<UUID> reachedCampIds = new HashSet<>();
    private BukkitTask tickTask;

    private double baseHealth;
    private double moveSpeed;
    private int spawnIntervalTicks;
    private int attackersPerWave;
    private double campDamagePerAttacker;

    public WaveManager(TowerDefensePlugin plugin) {
        this.plugin = plugin;
        var waves = plugin.getConfig().getConfigurationSection("waves");
        baseHealth = waves.getDouble("attacker-base-health", 100);
        moveSpeed = waves.getDouble("attacker-move-speed", 0.12);
        spawnIntervalTicks = waves.getInt("spawn-interval-ticks", 60);
        attackersPerWave = waves.getInt("attackers-per-wave", 8);
        campDamagePerAttacker = waves.getDouble("camp-damage-per-attacker", 5);
    }

    public void start() {
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (tickTask != null) tickTask.cancel();
    }

    /** Starts a wave for one player's plot (e.g. triggered from the Tower Panel). */
    public void startWave(UUID owner) {
        PlayerData data = plugin.getPlayerDataManager().get(owner);
        data.setAttacksPaused(false);
        spawnedThisWave.put(owner, 0);
        activeByOwner.computeIfAbsent(owner, k -> new ArrayList<>());
    }

    public void clearWave(UUID owner) {
        List<AttackerInstance> list = activeByOwner.get(owner);
        if (list != null) {
            for (AttackerInstance ai : list) {
                var e = plugin.getServer().getEntity(ai.getEntityId());
                if (e != null) e.remove();
            }
            list.clear();
        }
        spawnedThisWave.remove(owner);
    }

    private void tick() {
        long currentTick = plugin.getServer().getCurrentTick();

        for (UUID owner : new ArrayList<>(activeByOwner.keySet())) {
            PlayerData data = plugin.getPlayerDataManager().get(owner);
            if (data.isAttacksPaused()) continue;

            Plot plot = plugin.getPlotManager().getActivePlot(owner);
            if (plot == null) continue;

            maybeSpawn(owner, plot, currentTick);
            List<AttackerInstance> attackers = activeByOwner.get(owner);
            moveAttackers(owner, plot, attackers);

            List<LivingEntity> alive = new ArrayList<>();
            for (AttackerInstance ai : attackers) {
                var e = plugin.getServer().getEntity(ai.getEntityId());
                if (e instanceof LivingEntity le && !le.isDead()) alive.add(le);
            }
            plugin.getTowerManager().tickCombat(owner, currentTick, alive);

            cleanupDead(owner, attackers);
        }
    }

    private void maybeSpawn(UUID owner, Plot plot, long currentTick) {
        int spawned = spawnedThisWave.getOrDefault(owner, 0);
        if (spawned >= attackersPerWave) return;
        if (currentTick % spawnIntervalTicks != 0) return;

        Location spawnLoc = plot.toWorld(plot.getPathWaypointsRelative().get(0));
        Zombie zombie = spawnLoc.getWorld().spawn(spawnLoc, Zombie.class);
        zombie.setAI(false);
        zombie.setSilent(true);
        zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth);
        zombie.setHealth(baseHealth);
        zombie.setCustomNameVisible(true);
        updateHealthBar(zombie);

        AttackerInstance instance = new AttackerInstance(zombie.getUniqueId(), owner);
        activeByOwner.computeIfAbsent(owner, k -> new ArrayList<>()).add(instance);
        spawnedThisWave.put(owner, spawned + 1);
    }

    private void moveAttackers(UUID owner, Plot plot, List<AttackerInstance> attackers) {
        List<double[]> waypoints = plot.getPathWaypointsRelative();
        for (AttackerInstance ai : attackers) {
            var entity = plugin.getServer().getEntity(ai.getEntityId());
            if (!(entity instanceof LivingEntity le) || le.isDead()) continue;

            int idx = ai.getWaypointIndex();
            if (idx >= waypoints.size() - 1) {
                // reached the camp
                plugin.getCampManager().damageCamp(owner, campDamagePerAttacker);
                reachedCampIds.add(le.getUniqueId());
                le.remove();
                continue;
            }

            Location target = plot.toWorld(waypoints.get(idx + 1));
            Location current = le.getLocation();
            double dx = target.getX() - current.getX();
            double dz = target.getZ() - current.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < 0.3) {
                ai.setWaypointIndex(idx + 1);
            } else {
                double nx = current.getX() + (dx / dist) * moveSpeed;
                double nz = current.getZ() + (dz / dist) * moveSpeed;
                Location next = new Location(current.getWorld(), nx, target.getY(), nz);
                next.setDirection(new org.bukkit.util.Vector(dx, 0, dz));
                le.teleport(next);
            }

            updateHealthBar(le);
        }
    }

    private void updateHealthBar(LivingEntity le) {
        double pct = (le.getHealth() / le.getAttribute(Attribute.MAX_HEALTH).getBaseValue()) * 100.0;
        le.setCustomName("§a" + progressBar(pct) + " §f" + Math.round(pct) + "%");
    }

    private String progressBar(double percent) {
        int totalBars = 10;
        int filled = (int) Math.round((percent / 100.0) * totalBars);
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < totalBars; i++) {
            sb.append(i < filled ? "§a|" : "§c|");
        }
        return sb.toString();
    }

    private void cleanupDead(UUID owner, List<AttackerInstance> attackers) {
        Iterator<AttackerInstance> it = attackers.iterator();
        while (it.hasNext()) {
            AttackerInstance ai = it.next();
            var e = plugin.getServer().getEntity(ai.getEntityId());
            if (e == null || e.isDead()) {
                if (reachedCampIds.remove(ai.getEntityId())) {
                    // removed because it reached the camp, not a tower kill -> no payout
                } else {
                    // killed by a tower -> pay out
                    plugin.getPlayerDataManager().get(owner).addCoins(10);
                }
                it.remove();
            }
        }
    }
}
