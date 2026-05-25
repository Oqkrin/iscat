package uni.gaben.iscat.iscat_game.universe;

import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_game.camera.CameraModel;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyWaveController {

    private double spawnTimer = 0.0;
    private final Random random = new Random();

    private boolean bossSpawned = false;
    private boolean bossDead = false;

    private static final double SPAWN_RADIUS = 800.0;
    private static final double INITIAL_SPAWN_COOLDOWN = 15.0;
    private static final double PROGRESSION_STEP_SECONDS = 30.0;

    /**
     * Main update tick fueled directly by the shared GameModel timer state.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null) return;

        // Fetch the absolute unified master time from the model
        double masterTimeSec = gameModel.getTotalElapsedSeconds();

        // Count down the local intermediate delta step for spawn pacing
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnWaveOffScreen(camera, masterTimeSec);
            // Scale spawn intervals dynamically based on global engine runtime
            spawnTimer = Math.max(6.0, INITIAL_SPAWN_COOLDOWN - (masterTimeSec / 60.0));
        }
    }

    private void spawnWaveOffScreen(CameraModel camera, double currentSessionTime) {
        if (camera == null) return;

        int waveMultiplier = 1 + ((int) (currentSessionTime / PROGRESSION_STEP_SECONDS));
        boolean bossSpawnedThisWave = false;

        for (int w = 0; w < waveMultiplier; w++) {
            UniverseSpawnable enemyToSpawn = chooseEnemyTypeByTime(currentSessionTime);

            if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER) {
                if (bossSpawned || bossSpawnedThisWave) {
                    enemyToSpawn = UniverseSpawnable.ISCAT_MOB;
                } else {
                    bossSpawnedThisWave = true;
                }
            }

            int groupSize = getGroupSize(enemyToSpawn, currentSessionTime);

            for (int i = 0; i < groupSize; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double spawnX = camera.getX() + Math.cos(angle) * SPAWN_RADIUS;
                double spawnY = camera.getY() + Math.sin(angle) * SPAWN_RADIUS;

                UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);
            }

            if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER && !bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    private int getGroupSize(UniverseSpawnable type, double currentSessionTime) {
        return switch (type) {
            case ISCAT_MOB -> {
                if (currentSessionTime >= 120.0) yield 6;
                if (currentSessionTime >= 30.0)  yield 3;
                yield 1;
            }
            case EATER -> {
                if (currentSessionTime >= 120.0) yield 5;
                if (currentSessionTime >= 60.0)  yield 3;
                yield 1;
            }
            case FAKE_ISCAT -> (currentSessionTime >= 90.0) ? 3 : 1;
            case ISCAT_BOMBER -> 2;
            case ISCAT_MASTER, FALLEN_STAR_GOLEM, ISCAT_CORE, ISCAT_MOTHER, WORM -> 1;
            default -> 1;
        };
    }

    private UniverseSpawnable chooseEnemyTypeByTime(double currentSessionTime) {
        if (!bossSpawned && currentSessionTime >= 180.0) {
            return UniverseSpawnable.ISCAT_MASTER;
        }

        List<UniverseSpawnable> unlockedEnemies = new ArrayList<>();
        unlockedEnemies.add(UniverseSpawnable.ISCAT_MOB);

        if (currentSessionTime >= 30.0) unlockedEnemies.add(UniverseSpawnable.EATER);
        if (currentSessionTime >= 60.0) unlockedEnemies.add(UniverseSpawnable.ISCAT_BOMBER);
        if (currentSessionTime >= 90.0) {
            unlockedEnemies.add(UniverseSpawnable.FALLEN_STAR_GOLEM);
            unlockedEnemies.add(UniverseSpawnable.FAKE_ISCAT);
        }
        if (currentSessionTime >= 120.0) unlockedEnemies.add(UniverseSpawnable.ISCAT_CORE);
        if (currentSessionTime >= 150.0) unlockedEnemies.add(UniverseSpawnable.WORM);
        if (currentSessionTime >= 200.0) unlockedEnemies.add(UniverseSpawnable.ISCAT_MOTHER);

        return unlockedEnemies.get(random.nextInt(unlockedEnemies.size()));
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