package uni.gaben.iscat.game.controller;

import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.universe.UniverseSpawnable;
import uni.gaben.iscat.game.universe.UniverseSpawner;
import uni.gaben.iscat.game.view.camera.CameraModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyWaveController {

    private double timeElapsedSec = 0.0;
    private double spawnTimer = 0.0;
    private final Random random = new Random();

    private boolean bossSpawned = false;

    private static final double SPAWN_RADIUS  = 800.0;
    private static final double SPAWN_COOLDOWN = 15.0;

    public void update(double dt, CameraModel camera) {
        timeElapsedSec += dt;

        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnWaveOffScreen(camera);
            spawnTimer = SPAWN_COOLDOWN;
        }
    }

    private void spawnWaveOffScreen(CameraModel camera) {
        if (camera == null) return;

        int waveMultiplier = 1 + ((int) (timeElapsedSec / 30.0));

        for (int w = 0; w < waveMultiplier; w++) {
            UniverseSpawnable enemyToSpawn = chooseEnemyTypeByTime();
            int groupSize = getGroupSize(enemyToSpawn);

            for (int i = 0; i < groupSize; i++) {
                double angle  = random.nextDouble() * Math.PI * 2.0;
                double spawnX = camera.getX() + Math.cos(angle) * SPAWN_RADIUS;
                double spawnY = camera.getY() + Math.sin(angle) * SPAWN_RADIUS;
                UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);
            }

            if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER) {
                notifyBossSpawned();
                return;
            }
        }
    }

    private int getGroupSize(UniverseSpawnable type) {
        return switch (type) {
            case ISCAT_MOB -> {
                if (timeElapsedSec >= 120.0) yield 5;
                if (timeElapsedSec >= 30.0)  yield 3;
                yield 1;
            }
            case EATER -> {
                if (timeElapsedSec >= 120.0) yield 7;
                if (timeElapsedSec >= 60.0)  yield 5;
                yield 1;
            }
            case FAKE_ISCAT  -> (timeElapsedSec >= 90.0) ? 3 : 1;
            case ISCAT_BOMBER -> 2;
            case ISCAT_MASTER -> 1;
            case FALLEN_STAR_GOLEM, ISCAT_CORE, ISCAT_MOTHER, WORM -> 1;
            default -> 1;
        };
    }

    private UniverseSpawnable chooseEnemyTypeByTime() {
        if (!bossSpawned && timeElapsedSec >= 1.0) {
            return UniverseSpawnable.ISCAT_MASTER;
        }

        List<UniverseSpawnable> unlockedEnemies = new ArrayList<>();

        unlockedEnemies.add(UniverseSpawnable.ISCAT_MOB);

        if (timeElapsedSec >= 30.0) {
            unlockedEnemies.add(UniverseSpawnable.EATER);
        }
        if (timeElapsedSec >= 60.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_BOMBER);
        }
        if (timeElapsedSec >= 90.0) {
            unlockedEnemies.add(UniverseSpawnable.FALLEN_STAR_GOLEM);
            unlockedEnemies.add(UniverseSpawnable.FAKE_ISCAT);
        }
        if (timeElapsedSec >= 120.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_CORE);
        }
        if (timeElapsedSec >= 150.0) {
            unlockedEnemies.add(UniverseSpawnable.WORM);
        }

        if (timeElapsedSec >= 200.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_MOTHER);
        }

        int randomIndex = random.nextInt(unlockedEnemies.size());
        return unlockedEnemies.get(randomIndex);
    }

    public void notifyBossSpawned() {
        bossSpawned = true;
        IscatAudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
    }

    public void notifyBossDead() {
        IscatAudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
    }
}