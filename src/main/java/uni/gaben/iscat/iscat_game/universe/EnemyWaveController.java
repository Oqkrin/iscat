package uni.gaben.iscat.iscat_game.universe;

import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_game.camera.CameraModel;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;

import java.util.Random;

/**
 * Advanced Wave Controller utilizing mathematical time-based and level-based probability curves
 * with an exponential scaling multiplier based on the player's level.
 */
public class EnemyWaveController {

    private double spawnTimer = 0.0;
    private final Random random = new Random();

    private boolean bossSpawned = false;
    private boolean bossDead = false;

    // Configuration Constants
    private static final double SPAWN_RADIUS = 1200.0;
    private static final double INITIAL_SPAWN_COOLDOWN = 10.0;

    /**
     * Main update tick fueled directly by the shared GameModel timer state.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null) return;

        double masterTimeSec = gameModel.getTotalElapsedSeconds();

        // Count down the local pacing interval timer
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            // Extract player level safely with structural fallbacks
            int playerLevel = 1;
            if (gameModel.getUniverseModel() != null && gameModel.getUniverseModel().getPlayer() != null) {
                playerLevel = gameModel.getUniverseModel().getPlayer().getLevel();
            }

            spawnDynamicWaveOffScreen(camera, masterTimeSec, playerLevel);

            // Scale spawn frequencies up slightly over time (down to a minimum of 4 seconds)
            spawnTimer = Math.max(4.0, INITIAL_SPAWN_COOLDOWN - (masterTimeSec / 60.0));
        }
    }

    /**
     * Handles spawning calculated arrays of entities just outside the viewport camera bounds.
     */
    private void spawnDynamicWaveOffScreen(CameraModel camera, double currentSessionTime, int playerLevel) {
        if (camera == null) return;

        // Exponential budget rule: level * level total spawns (Level 10 = 100 enemies!)
        int totalEnemiesToSpawn = (int) (playerLevel * (1+Math.log(playerLevel)));
        boolean bossSpawnedThisWave = false;

        for (int i = 0; i < totalEnemiesToSpawn; i++) {
            // FIX: Now passing player level into probability calculations
            UniverseSpawnable enemyToSpawn = rollWeightProbability(currentSessionTime, playerLevel);

            // Boss Check: Safely downscale duplicated master triggers to preserve balance
            if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER) {
                if (bossSpawned || bossSpawnedThisWave) {
                    enemyToSpawn = UniverseSpawnable.ISCAT_MOB;
                } else {
                    bossSpawnedThisWave = true;
                }
            }

            // Calculate precise circular spawn trajectories outside the camera frame
            double angle = random.nextDouble() * Math.PI * 2.0;
            double spawnX = camera.getX() + Math.cos(angle) * SPAWN_RADIUS;
            double spawnY = camera.getY() + Math.sin(angle) * SPAWN_RADIUS;

            UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);

            // Notify environmental sound system if the final boss is selected
            if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER && !bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    /**
     * Dual-layered scaling system. Enemies unlock if EITHER the time threshold is reached
     * OR the player's level is high enough.
     */
    private UniverseSpawnable rollWeightProbability(double time, int level) {
        // Base mob drops in priority as the run escalates
        double mobWeight = Math.max(10.0, 100.0 - (time * 0.5) - (level * 4.0));

        // FIX: Unlock based on Time OR Player Level thresholds
        double eaterWeight  = (time >= 20.0  || level >= 2) ? Math.min(45.0, (time - 20.0) * 1.5 + (level * 3.0)) : 0.0;
        double fakeWeight   = (time >= 45.0  || level >= 3) ? Math.min(40.0, (time - 45.0) * 1.5 + (level * 3.5)) : 0.0;
        double bomberWeight = (time >= 7000.0  || level >= 4) ? Math.min(35.0, (time - 70.0) * 1.2 + (level * 4.0)) : 0.0;
        double golemWeight  = (time >= 100.0 || level >= 5) ? Math.min(30.0, (time - 100.0) * 1.0 + (level * 4.5)) : 0.0;
        double coreWeight   = (time >= 130.0 || level >= 6) ? Math.min(30.0, (time - 130.0) * 1.0 + (level * 5.0)) : 0.0;
        double wormWeight   = (time >= 160.0 || level >= 7) ? Math.min(25.0, (time - 160.0) * 0.8 + (level * 5.5)) : 0.0;
        double motherWeight = (time >= 190.0 || level >= 8) ? Math.min(25.0, (time - 190.0) * 0.8 + (level * 6.0)) : 0.0;

        // Final Boss threshold opens at 4 minutes OR Level 10
        double masterWeight = (!bossSpawned && (time >= 240.0 || level >= 10))
                ? Math.min(20.0, (time - 240.0) * 0.5 + (level * 2.0))
                : 0.0;

        // Sum complete active weight profile boundaries
        double totalWeightSum = mobWeight + eaterWeight + fakeWeight + bomberWeight
                + golemWeight + coreWeight + wormWeight + motherWeight + masterWeight;

        // Roll pointer index inside range space
        double rollValue = random.nextDouble() * totalWeightSum;
        double currentInspectedSum = 0.0;

        if ((currentInspectedSum += mobWeight) >= rollValue)    return UniverseSpawnable.ISCAT_MOB;
        if ((currentInspectedSum += eaterWeight) >= rollValue)  return UniverseSpawnable.EATER;
        if ((currentInspectedSum += fakeWeight) >= rollValue)   return UniverseSpawnable.FAKE_ISCAT;
        if ((currentInspectedSum += bomberWeight) >= rollValue) return UniverseSpawnable.ISCAT_BOMBER;
        if ((currentInspectedSum += golemWeight) >= rollValue)  return UniverseSpawnable.FALLEN_STAR_GOLEM;
        if ((currentInspectedSum += coreWeight) >= rollValue)   return UniverseSpawnable.ISCAT_CORE;
        if ((currentInspectedSum += wormWeight) >= rollValue)   return UniverseSpawnable.WORM;
        if ((currentInspectedSum += motherWeight) >= rollValue) return UniverseSpawnable.ISCAT_MOTHER;
        if ((currentInspectedSum += masterWeight) >= rollValue) return UniverseSpawnable.ISCAT_MASTER;

        return UniverseSpawnable.ISCAT_MOB; // Absolute safety line fallback
    }

    public void notifyBossSpawned() {
        if (!bossSpawned) {
            bossSpawned = true;
            bossDead = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
        }
    }

    public void notifyBossDead() {
        if (bossSpawned && !bossDead) {
            bossDead = true;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        }
    }

    public void reset() {
        this.spawnTimer = 0.0;
        this.bossSpawned = false;
        this.bossDead = false;
    }
}