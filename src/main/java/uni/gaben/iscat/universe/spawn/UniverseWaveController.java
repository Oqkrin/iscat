package uni.gaben.iscat.universe.spawn;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.model.BestiaryModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UniverseWaveController {

    private record ActiveEnemy(AbstractPhysicalEntityModel model, String enemyId) { }
    private record WaveConfig(ThreatLevel threatLevel, int totalEnemies) { }

    // ── Properties observable per HUD binding ─────────────────────────────────
    private static final IntegerProperty totalKillsProperty    = new SimpleIntegerProperty(0);
    private final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);
    private final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);

    // Proprietà osservabile per il testo della minaccia nella HUD
    private final StringProperty currentThreatLevelProperty = new SimpleStringProperty("LOW");

    public static IntegerProperty totalKillsProperty()     { return totalKillsProperty; }
    public IntegerProperty enemiesRemainingProperty()      { return enemiesRemainingProperty; }
    public IntegerProperty waveTotalProperty()             { return waveTotalProperty; }
    public IntegerProperty currentWaveProperty()           { return currentWaveProperty; }

    public int getEnemiesRemaining()   { return enemiesRemainingProperty.get(); }
    public int getWaveTotal()          { return waveTotalProperty.get(); }

    public static void incrementKills(AbstractPhysicalEntityModel entity) {
        if (entity == null) return;
        EntityRecord record = entity.getEntityRecord();
        if (record == null || record.threatLevel() == null || record.threatLevel() == ThreatLevel.NONE) return;
        totalKillsProperty.set(totalKillsProperty.get() + 1);
    }

    // ── Stato interno ─────────────────────────────────────────────────────────
    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();
    private final List<WaveConfig> fileWaves = new ArrayList<>();
    private Runnable onBossDeadCallback;

    private final Random random       = new Random();
    private boolean bossSpawned       = false;
    private boolean bossDead          = false;

    private double bossDeadTimer             = 0.0;
    private boolean bossCallbackTriggered    = false;

    private int currentWave                  = 1;
    private ThreatLevel currentThreatLevel   = ThreatLevel.LOW;
    private int totalEnemiesToSpawnThisWave  = 0;
    private int enemiesSpawnedThisWave       = 0;

    private boolean inIntermission    = true;
    private double  intermissionTimer = 5.0;
    private double  spawnTimer        = 0.0;

    private static final double SPAWN_DELAY           = 1.5;
    private static final double INTERMISSION_DURATION = 5.0;
    private static final double FIRST_WAVE_DELAY      = 5.0;

    private final BestiaryModel bestiaryModel = new BestiaryModel();

    // -------------------------------------------------------------------------
    // Caricamento file delle ondate
    // -------------------------------------------------------------------------
    public void loadWavesFromResource(String resourcePath) {
        fileWaves.clear();

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[WAVE CONTROLLER] File non trovato in: " + resourcePath + ". Uso logica procedurale.");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        try {
                            ThreatLevel level = ThreatLevel.valueOf(parts[0].trim().toUpperCase());
                            int count = Integer.parseInt(parts[1].trim());
                            fileWaves.add(new WaveConfig(level, count));
                        } catch (IllegalArgumentException e) {
                            System.err.println("[WAVE CONTROLLER] Errore di sintassi riga: " + line);
                        }
                    }
                }
                System.out.printf("[WAVE CONTROLLER] Configurazione caricata! Rilevate %d ondate custom.%n", fileWaves.size());

                // Se abbiamo appena caricato il file e siamo alla wave 1, aggiorna subito il testo per la HUD
                if (!fileWaves.isEmpty() && currentWave == 1) {
                    currentThreatLevel = fileWaves.get(0).threatLevel();
                    currentThreatLevelProperty.set(currentThreatLevel.name());
                }
            }
        } catch (IOException e) {
            System.err.println("[WAVE CONTROLLER] Errore di lettura: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Update principale
    // -------------------------------------------------------------------------
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());
        enemiesRemainingProperty.set(activeEnemies.size());

        if (bossDead && !bossCallbackTriggered) {
            bossDeadTimer -= dt;
            if (bossDeadTimer <= 0) {
                bossCallbackTriggered = true;
                if (onBossDeadCallback != null) {
                    onBossDeadCallback.run();
                }
            }
        }

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
        int waveIndex          = currentWave - 1;

        if (!fileWaves.isEmpty() && waveIndex < fileWaves.size()) {
            WaveConfig config           = fileWaves.get(waveIndex);
            currentThreatLevel          = config.threatLevel();
            totalEnemiesToSpawnThisWave = config.totalEnemies();

            System.out.printf("[WAVE CONTROLLER] Avviata WAVE CUSTOM %d | Minaccia: %s | Nemici: %d%n",
                    currentWave, currentThreatLevel.name(), totalEnemiesToSpawnThisWave);
        }
        else {
            if (currentWave <= 2) {
                currentThreatLevel = ThreatLevel.LOW;
            } else if (currentWave <= 4) {
                currentThreatLevel = ThreatLevel.NORMAL;
            } else if (currentWave <= 6) {
                currentThreatLevel = ThreatLevel.HIGH;
            } else if (currentWave <= 9) {
                currentThreatLevel = ThreatLevel.EXTREME;
            } else {
                currentThreatLevel = ThreatLevel.APOCALYPSE;
            }

            totalEnemiesToSpawnThisWave = switch (currentThreatLevel) {
                case LOW -> 3 + currentWave;
                case NORMAL -> 4 + currentWave;
                case HIGH -> 5 + currentWave;
                case EXTREME -> 6 + currentWave;
                default -> 3 + (currentWave * 2);
            };

            System.out.printf("[WAVE CONTROLLER] Avviata WAVE PROCEDURALE %d | Minaccia: %s | Nemici: %d%n",
                    currentWave, currentThreatLevel.name(), totalEnemiesToSpawnThisWave);
        }

        // Notifica la modifica della stringa alla proprietà osservabile
        currentThreatLevelProperty.set(currentThreatLevel.name());

        if (currentThreatLevel == ThreatLevel.APOCALYPSE) {
            System.out.println("[WAVE CONTROLLER] !!! APOCALYPSE DETECTED: IMMINENT BOSS SPAWN !!!");
        }

        AudioManager.getInstance().playSFX("alarm");

        waveTotalProperty.set(totalEnemiesToSpawnThisWave);
        enemiesRemainingProperty.set(totalEnemiesToSpawnThisWave);
        currentWaveProperty.set(currentWave);
    }

    private void endCurrentWave() {
        System.out.printf("[WAVE CONTROLLER] WAVE %d COMPLETATA! Preparazione prossima ondata...%n", currentWave);
        inIntermission    = true;
        intermissionTimer = INTERMISSION_DURATION;
        currentWave++;

        int nextWaveIndex = currentWave - 1;
        if (!fileWaves.isEmpty() && nextWaveIndex < fileWaves.size()) {
            currentThreatLevelProperty.set(fileWaves.get(nextWaveIndex).threatLevel().name());
        }
    }

    private void spawnSingleEnemyOffScreen(CameraModel camera, GameModel gameModel) {
        if (camera == null) return;

        String enemyIdToSpawn = selectEligibleEnemyId();

        double angle = random.nextDouble() * Math.PI * 2.0;
        double spawnDistance = 28.0 + random.nextDouble() * 10.0;

        double spawnX = camera.getX() + Math.cos(angle) * spawnDistance;
        double spawnY = camera.getY() + Math.sin(angle) * spawnDistance;

        Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyIdToSpawn, spawnX, spawnY);

        if (spawnedObject instanceof AbstractPhysicalEntityModel enemyModel) {
            if (enemyModel instanceof AbstractLivingEntityModel livingModel) {
                double difficultyMult = switch (currentThreatLevel) {
                    case LOW -> 1.0;
                    case NORMAL -> 1.25;
                    case HIGH -> 1.5;
                    case EXTREME -> 2.5;
                    case APOCALYPSE -> 3.0;
                    default -> 1.0;
                };

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

        if (currentThreatLevel == ThreatLevel.EXTREME) {
            pool.add("WORM");
        }

        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key       = entry.getKey().toLowerCase().trim();
            EntityRecord rec = entry.getValue();

            if (key.contains("player") || "iscat_master".equals(key)) continue;

            if (key.contains("worm")) {
                if (key.contains("tail") || key.contains("body") || key.contains("head") || key.contains("segment")) {
                    continue;
                }
            }

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
            bossDeadTimer = 3.0;
            bossCallbackTriggered = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
        }
    }

    public void reset() {
        spawnTimer                  = 0.0;
        bossSpawned                 = false;
        bossDead                    = false;
        bossDeadTimer               = 0.0;
        bossCallbackTriggered       = false;
        currentWave                 = 1;

        // Se resettiamo e il file è già stato caricato, mostra subito la minaccia della prima ondata custom
        if (!fileWaves.isEmpty()) {
            currentThreatLevel = fileWaves.get(0).threatLevel();
        } else {
            currentThreatLevel = ThreatLevel.LOW;
        }
        currentThreatLevelProperty.set(currentThreatLevel.name());

        enemiesSpawnedThisWave      = 0;
        totalEnemiesToSpawnThisWave = 0;
        inIntermission              = true;
        intermissionTimer           = FIRST_WAVE_DELAY;
        activeEnemies.clear();
        totalKillsProperty.set(0);
        enemiesRemainingProperty.set(0);
        waveTotalProperty.set(0);
        currentWaveProperty.set(1);
    }

    @Deprecated
    public String getCurrentThreatLevelDisplay() {
        return "Minaccia: " + currentThreatLevel.name();
    }

    public void setOnBossDeadCallback(Runnable callback) { this.onBossDeadCallback = callback; }
    public int         getCurrentWave()        { return currentWave; }
}