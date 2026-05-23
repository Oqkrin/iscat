package uni.gaben.iscat.game.controller;

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

    private static final double SPAWN_RADIUS = 800.0;
    private static final double SPAWN_COOLDOWN = 5.0;

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

        // Determina quanti gruppi/ondate lanciare contemporaneamente (aumenta ogni 30 secondi)
        int waveMultiplier = 1 + ((int) (timeElapsedSec / 30.0));

        for (int w = 0; w < waveMultiplier; w++) {

            // Estrai il tipo di nemico per questo gruppo
            UniverseSpawnable enemyToSpawn = chooseEnemyTypeByTime();

            // Determina la dimensione del gruppo per questo specifico nemico
            int groupSize = getGroupSize(enemyToSpawn);

            // Spawna l'intero gruppo in un punto coordinato della mappa
            for (int i = 0; i < groupSize; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double spawnX = camera.getX() + Math.cos(angle) * SPAWN_RADIUS;
                double spawnY = camera.getY() + Math.sin(angle) * SPAWN_RADIUS;

                UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);
            }
        }
    }

    private int getGroupSize(UniverseSpawnable type) {
        return switch (type) {
            case ISCAT_MOB -> {
                if (timeElapsedSec >= 120.0) yield 5;
                if (timeElapsedSec >= 30.0) yield 3;
                yield 1;
            }
            case EATER -> {
                if (timeElapsedSec >= 120.0) yield 7;
                if (timeElapsedSec >= 60.0) yield 5;
                yield 1;
            }
            case FAKE_ISCAT -> (timeElapsedSec >= 90.0) ? 3 : 1;
            case ISCAT_BOMBER -> 2;
            case FALLEN_STAR_GOLEM, ISCAT_CORE, ISCAT_MOTHER, WORM -> 1;
            default -> 1;
        };
    }

    private UniverseSpawnable chooseEnemyTypeByTime() {
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
        if (timeElapsedSec >= 200.0) {
            unlockedEnemies.add(UniverseSpawnable.WORM);
        }

        if (timeElapsedSec >= 300.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_MOTHER);
        }

        int randomIndex = random.nextInt(unlockedEnemies.size());
        return unlockedEnemies.get(randomIndex);
    }
}