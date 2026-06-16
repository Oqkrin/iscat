package uni.gaben.iscat.universe;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.model.game.GameModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Controllore dei flussi e delle ondate di nemici basato sui Livelli di Minaccia (ThreatLevel).
 * Gestisce la progressione a ondate sequenziali, lo spawn cadenzato a tempo fuori schermo,
 * e il blocco del flusso fino al totale abbattimento dei nemici dell'ondata precedente.
 */
public class UniverseWaveController {

    private record ActiveEnemy(AbstractEntityModel model, String enemyId) { }

    // ── Properties observable per HUD binding ─────────────────────────────────

    /** Kills totali della sessione (solo nemici con ThreatLevel reale, no proiettili/asteroidi). */
    private static final IntegerProperty totalKillsProperty    = new SimpleIntegerProperty(0);

    /** Nemici ancora vivi nell'ondata corrente. */
    private final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);

    /** Totale nemici da spawnare nell'ondata corrente. */
    private final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);

    /** Numero dell'ondata corrente. */
    private final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);

    public static IntegerProperty totalKillsProperty()     { return totalKillsProperty; }
    public IntegerProperty enemiesRemainingProperty()      { return enemiesRemainingProperty; }
    public IntegerProperty waveTotalProperty()             { return waveTotalProperty; }
    public IntegerProperty currentWaveProperty()           { return currentWaveProperty; }

    public static int getTotalKills()  { return totalKillsProperty.get(); }
    public int getEnemiesRemaining()   { return enemiesRemainingProperty.get(); }
    public int getWaveTotal()          { return waveTotalProperty.get(); }

    /**
     * Incrementa i kill solo se l'entità è un nemico "reale" (ThreatLevel != NONE).
     * Proiettili, asteroidi e qualunque entità hardcoded senza EntityRecord vengono ignorati.
     */
    public static void incrementKills(AbstractEntityModel entity) {
        if (entity == null) return;

        // Le entità hardcoded (proiettili, asteroidi) hanno entity == null o threatLevel NONE
        EntityRecord record = entity.getEntityRecord();
        if (record == null || record.threatLevel() == null || record.threatLevel() == ThreatLevel.NONE) return;

        totalKillsProperty.set(totalKillsProperty.get() + 1);
    }

    // ── Stato interno ─────────────────────────────────────────────────────────

    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();
    private Runnable onBossDeadCallback;

    public boolean forceBossSpawn = false;

    private final Random random       = new Random();
    private boolean bossSpawned       = false;
    private boolean bossDead          = false;

    private int currentWave                  = 1;
    private ThreatLevel currentThreatLevel   = ThreatLevel.LOW;
    private int totalEnemiesToSpawnThisWave  = 0;
    private int enemiesSpawnedThisWave       = 0;

    private boolean inIntermission    = true;
    private double  intermissionTimer = 3.0;
    private double  spawnTimer        = 0.0;

    private static final double SPAWN_DELAY           = 1.5;
    private static final double INTERMISSION_DURATION = 5.0;

    // -------------------------------------------------------------------------
    // Update principale
    // -------------------------------------------------------------------------

    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        // Pulizia nemici morti e aggiornamento property rimanenti
        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());
        enemiesRemainingProperty.set(activeEnemies.size());

        if (inIntermission) {
            intermissionTimer -= dt;
            if (intermissionTimer <= 0) {
                inIntermission = false;
                startNewWave();
            }
            return;
        }

        if (enemiesSpawnedThisWave < totalEnemiesToSpawnThisWave) {
            spawnTimer -= dt;
            if (spawnTimer <= 0) {
                spawnSingleEnemyOffScreen(camera, gameModel);
                spawnTimer = SPAWN_DELAY;
            }
        } else if (activeEnemies.isEmpty()) {
            endCurrentWave();
        }
    }

    // -------------------------------------------------------------------------
    // Wave management
    // -------------------------------------------------------------------------

    private void startNewWave() {
        enemiesSpawnedThisWave = 0;
        spawnTimer             = 0.0;

        if (forceBossSpawn || currentWave >= 5) {
            currentThreatLevel = ThreatLevel.APOCALYPSE;
        } else {
            currentThreatLevel = switch (currentWave) {
                case 1 -> ThreatLevel.LOW;
                case 2 -> ThreatLevel.NORMAL;
                case 3 -> ThreatLevel.HIGH;
                default -> ThreatLevel.EXTREME;
            };
        }

        if (currentThreatLevel == ThreatLevel.APOCALYPSE) {
            totalEnemiesToSpawnThisWave = 1;
            System.out.println("[WAVE CONTROLLER] !!! APOCALYPSE DETECTED: IMMINENT BOSS SPAWN !!!");
        } else {
            totalEnemiesToSpawnThisWave = 3 + (currentWave * 2);
            System.out.printf("[WAVE CONTROLLER] Avviata WAVE %d | Minaccia: %s | Nemici totali: %d%n",
                    currentWave, currentThreatLevel.name(), totalEnemiesToSpawnThisWave);
        }

        // Aggiorna subito le property HUD
        waveTotalProperty.set(totalEnemiesToSpawnThisWave);
        enemiesRemainingProperty.set(totalEnemiesToSpawnThisWave);
        currentWaveProperty.set(currentWave);
    }

    private void endCurrentWave() {
        System.out.printf("[WAVE CONTROLLER] WAVE %d COMPLETATA! Preparazione prossima ondata...%n", currentWave);
        inIntermission    = true;
        intermissionTimer = INTERMISSION_DURATION;
        currentWave++;
    }

    // -------------------------------------------------------------------------
    // Spawn
    // -------------------------------------------------------------------------

    private void spawnSingleEnemyOffScreen(CameraModel camera, GameModel gameModel) {
        if (camera == null) return;

        String enemyIdToSpawn = selectEligibleEnemyId();

        double margin    = 100.0;
        double halfWidth  = (camera.getScreenWidth()  / 2.0) + margin;
        double halfHeight = (camera.getScreenHeight() / 2.0) + margin;

        int    side   = random.nextInt(4);
        double spawnX = camera.getX();
        double spawnY = camera.getY();

        switch (side) {
            case 0 -> { spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2); spawnY = camera.getY() - halfHeight; }
            case 1 -> { spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2); spawnY = camera.getY() + halfHeight; }
            case 2 -> { spawnX = camera.getX() - halfWidth; spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2); }
            case 3 -> { spawnX = camera.getX() + halfWidth; spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2); }
        }

        Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyIdToSpawn, spawnX, spawnY);

        if (spawnedObject instanceof AbstractEntityModel enemyModel) {
            if (enemyModel instanceof AbstractLivingEntityModel livingModel) {
                double masterTimeSec = gameModel.getTotalElapsedSeconds();
                int playerLevel      = gameModel.getUniverseModel().getPlayer().getLevel();
                int difficultyMult   = (int) (playerLevel + currentWave + (masterTimeSec / 60));
                livingModel.setMaxEndurance(livingModel.getMaxEndurance() * difficultyMult);
                livingModel.setEndurance(livingModel.getMaxEndurance());
            }

            activeEnemies.add(new ActiveEnemy(enemyModel, enemyIdToSpawn));
            enemiesSpawnedThisWave++;

            // Aggiorna subito "rimanenti" per riflettere il nemico appena aggiunto
            enemiesRemainingProperty.set(activeEnemies.size());

            if ("ISCAT_MASTER".equalsIgnoreCase(enemyIdToSpawn) && !bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    private String selectEligibleEnemyId() {
        if (currentThreatLevel == ThreatLevel.APOCALYPSE) return "ISCAT_MASTER";

        List<String> pool = new ArrayList<>();
        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key       = entry.getKey();
            EntityRecord rec = entry.getValue();

            if (key.contains("player") || "ISCAT_MASTER".equalsIgnoreCase(key)) continue;

            if (rec.threatLevel() != null && rec.threatLevel() != ThreatLevel.NONE) {
                if (rec.threatLevel().ordinal() <= currentThreatLevel.ordinal()) {
                    pool.add(key);
                }
            }
        }

        return pool.isEmpty() ? "iscat_mob" : pool.get(random.nextInt(pool.size()));
    }

    // -------------------------------------------------------------------------
    // Boss callbacks
    // -------------------------------------------------------------------------

    public void notifyBossSpawned() {
        if (!bossSpawned) {
            bossSpawned = true;
            bossDead    = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
        }
    }

    public void notifyBossDead() {
        if (bossSpawned && !bossDead) {
            bossDead = true;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
            if (onBossDeadCallback != null) onBossDeadCallback.run();
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    public void reset() {
        spawnTimer                  = 0.0;
        bossSpawned                 = false;
        bossDead                    = false;
        currentWave                 = 1;
        currentThreatLevel          = ThreatLevel.LOW;
        enemiesSpawnedThisWave      = 0;
        totalEnemiesToSpawnThisWave = 0;
        inIntermission              = true;
        intermissionTimer           = 3.0;
        forceBossSpawn              = false;
        activeEnemies.clear();

        totalKillsProperty.set(0);
        enemiesRemainingProperty.set(0);
        waveTotalProperty.set(0);
        currentWaveProperty.set(1);
    }

    public void setOnBossDeadCallback(Runnable callback) { this.onBossDeadCallback = callback; }

    public int         getCurrentWave()        { return currentWave; }
    public ThreatLevel getCurrentThreatLevel() { return currentThreatLevel; }
}