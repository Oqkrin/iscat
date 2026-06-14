package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.ThreatLevel;
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

    private static class ActiveEnemy {
        final AbstractEntityModel model;
        final String enemyId;

        ActiveEnemy(AbstractEntityModel model, String enemyId) {
            this.model = model;
            this.enemyId = enemyId;
        }
    }

    /** Contatore statico delle uccisioni totali eseguite nella sessione corrente. */
    public static int totalKills = 0;

    /** Incrementa di un'unità il contatore globale dei nemici abbattuti. */
    public static void incrementKills() { totalKills++; }

    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();
    private Runnable onBossDeadCallback;

    /** Forza l'attivazione immediata del Boss finale al prossimo ciclo utile. */
    public boolean forceBossSpawn = false;

    private final Random random = new Random();
    private boolean bossSpawned = false;
    private boolean bossDead = false;

    private int currentWave = 1;
    private ThreatLevel currentThreatLevel = ThreatLevel.LOW;

    private int totalEnemiesToSpawnThisWave = 0;
    private int enemiesSpawnedThisWave = 0;

    private boolean inIntermission = true;
    private double intermissionTimer = 3.0; // Pausa iniziale prima della Wave 1
    private double spawnTimer = 0.0;

    private static final double SPAWN_DELAY = 1.5;            // Secondi di attesa tra lo spawn di un singolo nemico e il successivo
    private static final double INTERMISSION_DURATION = 5.0;  // Secondi di pausa tra la pulizia di una wave e l'inizio della successiva

    /**
     * Aggiorna lo stato temporale della gestione ondate.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        // Pulizia automatica dei nemici distrutti
        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());

        // Gestione del tempo di transizione tra un'ondata e l'altra
        if (inIntermission) {
            intermissionTimer -= dt;
            if (intermissionTimer <= 0) {
                inIntermission = false;
                startNewWave();
            }
            return;
        }

        // Fase 1: Continuo a spawnare se non ho raggiunto il tetto dell'ondata corrente
        if (enemiesSpawnedThisWave < totalEnemiesToSpawnThisWave) {
            spawnTimer -= dt;
            if (spawnTimer <= 0) {
                spawnSingleEnemyOffScreen(camera, gameModel);
                spawnTimer = SPAWN_DELAY; // Ripristina il cooldown tra i singoli spawn
            }
        }
        // Fase 2: Tutti i nemici sono stati iniettati. Controllo se l'utente li ha uccisi TUTTI
        else if (activeEnemies.isEmpty()) {
            endCurrentWave();
        }
    }

    /**
     * Configura e inizializza i parametri matematici e di difficoltà per la nuova ondata.
     */
    private void startNewWave() {
        enemiesSpawnedThisWave = 0;
        spawnTimer = 0.0; // Forza il primo spawn istantaneo all'avvio della wave

        // Calcolo del ThreatLevel basato sul numero dell'ondata
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

        // Calcolo del quantitativo di nemici (Cresce ad ogni ondata)
        if (currentThreatLevel == ThreatLevel.APOCALYPSE) {
            totalEnemiesToSpawnThisWave = 1; // Solo il Boss ad Apocalisse
            System.out.println("[WAVE CONTROLLER] !!! APOCALYPSE DETECTED: IMMINENT BOSS SPAWN !!!");
        } else {
            totalEnemiesToSpawnThisWave = 3 + (currentWave * 2); // Esempio: Wave 1 = 5 nemici, Wave 2 = 7 nemici, ecc.
            System.out.println(String.format("[WAVE CONTROLLER] Avviata WAVE %d | Minaccia: %s | Nemici totali: %d",
                    currentWave, currentThreatLevel.name(), totalEnemiesToSpawnThisWave));
        }
    }

    /**
     * Chiude l'ondata attuale e attiva il timer di riposo prima della successiva.
     */
    private void endCurrentWave() {
        System.out.println(String.format("[WAVE CONTROLLER] WAVE %d COMPLETATA! Preparazione prossima ondata...", currentWave));
        inIntermission = true;
        intermissionTimer = INTERMISSION_DURATION;
        currentWave++;
    }

    /**
     * Seleziona ed inietta un singolo nemico idoneo calcolando le coordinate esterne alla visuale.
     */
    private void spawnSingleEnemyOffScreen(CameraModel camera, GameModel gameModel) {
        if (camera == null) return;

        String enemyIdToSpawn = selectEligibleEnemyId();

        // Calcolo geometrico dei quadranti esterni allo schermo
        double margin = 100.0;
        double halfWidth = (camera.getScreenWidth() / 2.0) + margin;
        double halfHeight = (camera.getScreenHeight() / 2.0) + margin;

        int side = random.nextInt(4);
        double spawnX = camera.getX();
        double spawnY = camera.getY();

        switch (side) {
            case 0 -> { // Alto
                spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                spawnY = camera.getY() - halfHeight;
            }
            case 1 -> { // Basso
                spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                spawnY = camera.getY() + halfHeight;
            }
            case 2 -> { // Sinistra
                spawnX = camera.getX() - halfWidth;
                spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
            }
            case 3 -> { // Destra
                spawnX = camera.getX() + halfWidth;
                spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
            }
        }

        // Spawn tramite il motore centrale
        Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyIdToSpawn, spawnX, spawnY);

        if (spawnedObject instanceof AbstractEntityModel enemyModel) {
            // Scaling dinamico dei punti vita basato su ondata, livello giocatore e tempo trascorso
            if (enemyModel instanceof AbstractLivingEntityModel livingModel) {
                double masterTimeSec = gameModel.getTotalElapsedSeconds();
                int playerLevel = gameModel.getUniverseModel().getPlayer().getLevel();

                int difficultyMultiplier = (int) (playerLevel + currentWave + (masterTimeSec / 60));
                livingModel.setMaxEndurance(livingModel.getMaxEndurance() * difficultyMultiplier);
                livingModel.setEndurance(livingModel.getMaxEndurance());
            }

            activeEnemies.add(new ActiveEnemy(enemyModel, enemyIdToSpawn));
            enemiesSpawnedThisWave++;

            if ("ISCAT_MASTER".equalsIgnoreCase(enemyIdToSpawn) && !bossSpawned) {
                notifyBossSpawned();
            }
        }
    }

    /**
     * Filtra la cache delle entità e restituisce un ID compatibile con il livello di minaccia corrente.
     */
    private String selectEligibleEnemyId() {
        if (currentThreatLevel == ThreatLevel.APOCALYPSE) {
            return "ISCAT_MASTER";
        }

        List<String> pool = new ArrayList<>();

        // Analizziamo la cache per estrarre tutti i nemici conformi alle regole cumulative del ThreatLevel
        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key = entry.getKey();
            EntityRecord record = entry.getValue();

            // Escludiamo skin del giocatore ed il Boss finale dal pool comune
            if (key.contains("player") || "ISCAT_MASTER".equalsIgnoreCase(key)) {
                continue;
            }

            if (record.threatLevel() != null && record.threatLevel() != ThreatLevel.NONE) {
                // Regola cumulativa: il grado del nemico deve essere inferiore o uguale a quello della wave attuale
                if (record.threatLevel().ordinal() <= currentThreatLevel.ordinal()) {
                    pool.add(key);
                }
            }
        }

        // Fallback di sicurezza se la cache è vuota o nessun nemico corrisponde
        if (pool.isEmpty()) {
            return "iscat_mob";
        }

        return pool.get(random.nextInt(pool.size()));
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
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
            if (onBossDeadCallback != null) onBossDeadCallback.run();
        }
    }

    /**
     * Reset totale dei parametri per l'avvio di una nuova partita pulita.
     */
    public void reset() {
        this.spawnTimer = 0.0;
        this.bossSpawned = false;
        this.bossDead = false;
        this.currentWave = 1;
        this.currentThreatLevel = ThreatLevel.LOW;
        this.enemiesSpawnedThisWave = 0;
        this.totalEnemiesToSpawnThisWave = 0;
        this.inIntermission = true;
        this.intermissionTimer = 3.0;
        this.forceBossSpawn = false;
        totalKills = 0;
        activeEnemies.clear();
    }

    public void setOnBossDeadCallback(Runnable callback) {
        this.onBossDeadCallback = callback;
    }

    // Getter utili per mostrare i dati dell'ondata corrente nell'HUD di gioco
    public int getCurrentWave() { return currentWave; }
    public ThreatLevel getCurrentThreatLevel() { return currentThreatLevel; }
}