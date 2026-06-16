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
import uni.gaben.iscat.model.BestiaryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UniverseWaveController {

    private record ActiveEnemy(AbstractEntityModel model, String enemyId) { }

    // ── Properties observable per HUD binding ─────────────────────────────────
    private static final IntegerProperty totalKillsProperty    = new SimpleIntegerProperty(0);
    private final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);
    private final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);

    public static IntegerProperty totalKillsProperty()     { return totalKillsProperty; }
    public IntegerProperty enemiesRemainingProperty()      { return enemiesRemainingProperty; }
    public IntegerProperty waveTotalProperty()             { return waveTotalProperty; }
    public IntegerProperty currentWaveProperty()           { return currentWaveProperty; }

    public static int getTotalKills()  { return totalKillsProperty.get(); }
    public int getEnemiesRemaining()   { return enemiesRemainingProperty.get(); }
    public int getWaveTotal()          { return waveTotalProperty.get(); }

    public static void incrementKills(AbstractEntityModel entity) {
        if (entity == null) return;
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

    private final BestiaryModel bestiaryModel = new BestiaryModel();

    // -------------------------------------------------------------------------
    // Update principale
    // -------------------------------------------------------------------------
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

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

            enemiesRemainingProperty.set(activeEnemies.size());

            if ("ISCAT_MASTER".equalsIgnoreCase(enemyIdToSpawn) && !bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    private String selectEligibleEnemyId() {
        if (currentThreatLevel == ThreatLevel.APOCALYPSE) return "ISCAT_MASTER";

        List<String> pool = new ArrayList<>();

        int userId = 0;
        if (uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser() != null) {
            userId = uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser().id();
        }

        bestiaryModel.loadEnemies(userId);

        // INIEZIONE MANUALE DI WORM
        if (currentThreatLevel == ThreatLevel.EXTREME) {
            pool.add("WORM");
        }

        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key       = entry.getKey().toLowerCase().trim();
            EntityRecord rec = entry.getValue();

            if (key.contains("player") || "iscat_master".equals(key)) continue;

            // FILTRO PREVENTIVO SEGMENTI WORM
            if (key.contains("worm")) {
                if (key.contains("tail") || key.contains("body") || key.contains("head") || key.contains("segment")) {
                    continue;
                }
            }

            // REGOLA SPECIALE GOBLIN INVADER (spawna solo se il player ha ucciso il master almeno una volta)
            if ("goblin_invader".equals(key)) {
                if (!bestiaryModel.isUnlocked("iscat_master")) {
                    continue;
                }
            }

            if (rec.threatLevel() != null && rec.threatLevel() != ThreatLevel.NONE) {
                if (rec.threatLevel().ordinal() <= currentThreatLevel.ordinal()) {
                    pool.add(entry.getKey());
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