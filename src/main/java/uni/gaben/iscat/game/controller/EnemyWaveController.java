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

        // Se il timer scade, facciamo partorire i nemici dell'ondata
        if (spawnTimer <= 0) {
            spawnWaveOffScreen(camera);
            spawnTimer = SPAWN_COOLDOWN;
        }
    }

    private void spawnWaveOffScreen(CameraModel camera) {
        if (camera == null) return;

        // 1. Calcola quanti nemici spawnare in base alla formula dei 30 secondi:
        // A 0s: 1 + (0/30) = 1
        // A 20s: 1 + (30/30) = 2
        // A 40s: 1 + (60/30) = 3
        // A 60s: 1 + (90/30) = 4, ecc.
        int enemiesToSpawn = 1 + ((int) (timeElapsedSec / 30.0));

        // Ciclo per spawnare la quantità di nemici calcolata
        for (int i = 0; i < enemiesToSpawn; i++) {
            // Scegliamo un angolo casuale per ciascun nemico della stessa ondata
            double angle = random.nextDouble() * Math.PI * 2.0;
            double spawnX = camera.getX() + Math.cos(angle) * SPAWN_RADIUS;
            double spawnY = camera.getY() + Math.sin(angle) * SPAWN_RADIUS;

            // Scegli il tipo di nemico dal pool sbloccato
            UniverseSpawnable enemyToSpawn = chooseEnemyTypeByTime();

            // Evoca il nemico
            UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);
        }
    }

    /**
     * Ogni 30 secondi si sblocca un nuovo nemico
     */
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
        }

        if (timeElapsedSec >= 120.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_CORE);
        }

        if (timeElapsedSec >= 150.0) {
            unlockedEnemies.add(UniverseSpawnable.WORM);
        }

        if (timeElapsedSec >= 150.0) {
            unlockedEnemies.add(UniverseSpawnable.ISCAT_MOTHER);
        }

        // Estrae un indice a caso tra tutti i nemici correntemente sbloccati
        int randomIndex = random.nextInt(unlockedEnemies.size());
        return unlockedEnemies.get(randomIndex);
    }
}