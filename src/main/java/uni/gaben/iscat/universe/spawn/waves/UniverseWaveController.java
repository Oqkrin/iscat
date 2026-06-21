package uni.gaben.iscat.universe.spawn.waves;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.model.BestiaryModel;
import uni.gaben.iscat.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Controller orchestratore delle ondate (Wave System).
 * Gestisce i timer di intermissione, calcola i punti di spawn stocastici off-screen
 * e adatta i moltiplicatori di endurance dei nemici in base al livello di minaccia attuale.
 */
public class UniverseWaveController {

    private record ActiveEnemy(AbstractPhysicalEntityModel model, String enemyId) { }

    private static final double SPAWN_DELAY           = 1.5;
    private static final double INTERMISSION_DURATION = 5.0;

    private final WaveConfigManager configManager = new WaveConfigManager();
    private final WaveState state                 = new WaveState();
    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();
    private final Random random                   = new Random();
    private final BestiaryModel bestiaryModel     = new BestiaryModel();

    private Runnable onBossDeadCallback;

    // --- Proprietà JavaFX e Getters Reattivi ---
    public static IntegerProperty totalKillsProperty()     { return WaveState.totalKillsProperty(); }
    public IntegerProperty enemiesRemainingProperty()      { return state.enemiesRemainingProperty; }
    public IntegerProperty waveTotalProperty()             { return state.waveTotalProperty; }
    public IntegerProperty currentWaveProperty()           { return state.currentWaveProperty; }
    public StringProperty currentThreatLevelProperty()     { return state.currentThreatLevelProperty; }

    public int getEnemiesRemaining()                       { return state.enemiesRemainingProperty.get(); }
    public int getWaveTotal()                              { return state.waveTotalProperty.get(); }
    public int getCurrentWave()                            { return state.currentWave; }

    /**
     * Carica i descrittori delle ondate da file e imposta la minaccia di partenza.
     */
    public void loadWavesFromResource(String resourcePath) {
        configManager.loadWavesFromResource(resourcePath);
        if (!configManager.getLoadedWaves().isEmpty() && state.currentWave == 1) {
            state.currentThreatLevel = configManager.getLoadedWaves().getFirst().threatLevel();
            state.currentThreatLevelProperty.set(state.currentThreatLevel.name());
        }
    }

    /**
     * Incrementa il registro globale dei kill se l'entità possiede un livello di minaccia valido.
     */
    public static void incrementKills(AbstractPhysicalEntityModel entity) {
        if (entity == null) return;
        EntityRecord record = entity.getEntityRecord();
        if (record == null || record.threatLevel() == null || record.threatLevel() == ThreatLevel.NONE) return;
        totalKillsProperty().set(totalKillsProperty().get() + 1);
    }

    /**
     * Aggiorna la pipeline di spawn, i timer di round/intervallo e pulisce i puntatori ai nemici rimossi.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());
        state.enemiesRemainingProperty.set(activeEnemies.size());

        if (state.gameWon && !state.victoryCallbackTriggered) {
            state.victoryTimer -= dt;
            if (state.victoryTimer <= 0) {
                state.victoryCallbackTriggered = true;
                if (onBossDeadCallback != null) onBossDeadCallback.run();
            }
            return;
        }

        if (state.inIntermission) {
            state.intermissionTimer -= dt;
            if (state.intermissionTimer <= 0) {
                state.inIntermission = false;
                startNewWave();
            }
            return;
        }

        if (state.enemiesSpawnedThisWave < state.totalEnemiesToSpawnThisWave) {
            state.spawnTimer -= dt;
            if (state.spawnTimer <= 0) {
                spawnSingleEnemyOffScreen(camera, gameModel);
                state.spawnTimer = SPAWN_DELAY;
            }
        } else if (activeEnemies.isEmpty()) {
            endCurrentWave();
        }
    }

    /**
     * Configura i parametri ed avvia la nuova ondata (procedurale o da file).
     */
    private void startNewWave() {
        state.enemiesSpawnedThisWave = 0;
        state.spawnTimer             = 0.0;
        int waveIndex                = state.currentWave - 1;
        var loadedWaves              = configManager.getLoadedWaves();

        if (!loadedWaves.isEmpty() && waveIndex < loadedWaves.size()) {
            var config                     = loadedWaves.get(waveIndex);
            state.currentThreatLevel       = config.threatLevel();
            state.totalEnemiesToSpawnThisWave = config.totalEnemies();
        } else {
            if (state.currentWave <= 2) state.currentThreatLevel = ThreatLevel.LOW;
            else if (state.currentWave <= 4) state.currentThreatLevel = ThreatLevel.NORMAL;
            else if (state.currentWave <= 6) state.currentThreatLevel = ThreatLevel.HIGH;
            else if (state.currentWave <= 9) state.currentThreatLevel = ThreatLevel.EXTREME;
            else state.currentThreatLevel = ThreatLevel.APOCALYPSE;

            state.totalEnemiesToSpawnThisWave = switch (state.currentThreatLevel) {
                case LOW -> 3 + state.currentWave;
                case NORMAL -> 4 + state.currentWave;
                case HIGH -> 5 + state.currentWave;
                case EXTREME -> 6 + state.currentWave;
                default -> 3 + (state.currentWave * 2);
            };
        }

        state.currentThreatLevelProperty.set(state.currentThreatLevel.name());
        AudioManager.getInstance().playSFX("alarm");
        state.waveTotalProperty.set(state.totalEnemiesToSpawnThisWave);
        state.enemiesRemainingProperty.set(state.totalEnemiesToSpawnThisWave);
        state.currentWaveProperty.set(state.currentWave);
    }

    private void endCurrentWave() {
        var loadedWaves = configManager.getLoadedWaves();
        if (!loadedWaves.isEmpty() && state.currentWave >= loadedWaves.size()) {
            state.gameWon = true;
            state.victoryTimer = 3.0;
            state.victoryCallbackTriggered = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
            return;
        }

        state.inIntermission    = true;
        state.intermissionTimer = INTERMISSION_DURATION;
        state.currentWave++;

        int nextWaveIndex = state.currentWave - 1;
        if (!loadedWaves.isEmpty() && nextWaveIndex < loadedWaves.size()) {
            state.currentThreatLevelProperty.set(loadedWaves.get(nextWaveIndex).threatLevel().name());
        }
    }

    /**
     * Istanzia un nemico fuori dallo schermo applicandogli il moltiplicatore di endurance legato alla minaccia.
     */
    private void spawnSingleEnemyOffScreen(CameraModel camera, GameModel gameModel) {
        if (camera == null) return;
        String enemyIdToSpawn = selectEligibleEnemyId();

        var player = gameModel.getUniverseModel().getPlayer();
        double playerX = player != null ? UU.mToPx(player.getTransform().getTranslationX()) : camera.getX();
        double playerY = player != null ? UU.mToPx(player.getTransform().getTranslationY()) : camera.getY();

        double MIN_SPAWN_DISTANCE = 500;
        double spawnX, spawnY;
        int attempts = 0;

        do {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double spawnDistance = 1000 + random.nextDouble() * 10.0;
            spawnX = camera.getX() + Math.cos(angle) * spawnDistance;
            spawnY = camera.getY() + Math.sin(angle) * spawnDistance;
            attempts++;
        } while (Math.hypot(spawnX - playerX, spawnY - playerY) < MIN_SPAWN_DISTANCE && attempts < 10);

        Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyIdToSpawn, spawnX, spawnY);

        if (spawnedObject instanceof AbstractPhysicalEntityModel enemyModel) {
            if (enemyModel instanceof AbstractLivingEntityModel livingModel) {
                double difficultyMult = switch (state.currentThreatLevel) {
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
            state.enemiesSpawnedThisWave++;
            state.enemiesRemainingProperty.set(activeEnemies.size());

            if ("ISCAT_MASTER".equalsIgnoreCase(enemyIdToSpawn) && !state.bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    /**
     * Seleziona un ID nemico dal pool compatibile con l'attuale livello di minaccia ed il bestiario sbloccato.
     */
    private String selectEligibleEnemyId() {
        if (state.currentThreatLevel == ThreatLevel.APOCALYPSE) return "ISCAT_MASTER";

        List<String> pool = new ArrayList<>();
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().id() : 0;
        bestiaryModel.loadEnemies(userId);

        if (state.currentThreatLevel == ThreatLevel.EXTREME) pool.add("WORM");

        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key = entry.getKey().toLowerCase().trim();
            EntityRecord rec = entry.getValue();

            if (key.contains("player") || "iscat_master".equals(key)) continue;
            if (key.contains("worm") && (key.contains("tail") || key.contains("body") || key.contains("head") || key.contains("segment"))) continue;
            if ("goblin_invader".equals(key) && !bestiaryModel.isUnlocked("iscat_master")) continue;

            if (rec.threatLevel() != null && rec.threatLevel() != ThreatLevel.NONE) {
                if (rec.threatLevel().ordinal() <= state.currentThreatLevel.ordinal()) {
                    pool.add(entry.getKey());
                }
            }
        }
        return pool.isEmpty() ? "iscat_mob" : pool.get(random.nextInt(pool.size()));
    }

    public void notifyBossSpawned() {
        if (!state.bossSpawned) {
            state.bossSpawned = true;
            state.bossDead    = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
        }
    }

    public void notifyBossDead() {
        if (state.bossSpawned && !state.bossDead) {
            state.bossDead = true;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
        }
    }

    public void reset() {
        var loadedWaves = configManager.getLoadedWaves();
        ThreatLevel initialThreat = !loadedWaves.isEmpty() ? loadedWaves.getFirst().threatLevel() : ThreatLevel.LOW;
        state.reset(initialThreat);
        activeEnemies.clear();
    }

    public void setOnBossDeadCallback(Runnable callback) { this.onBossDeadCallback = callback; }
}