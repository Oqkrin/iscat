package uni.gaben.iscat.universe.spawn;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.model.BestiaryModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Gestore principale delle ondate del gioco (Wave Controller).
 * Si occupa del caricamento dei dati delle ondate da file JSON, dello spawn
 * dei nemici fuori dallo schermo in base al livello di minaccia corrente,
 * della gestione degli intervalli di intermissione e del rilevamento dello
 * stato di vittoria finale al completamento dell'ultima ondata.
 */
public class UniverseWaveController {

    /**
     * Record interno per tracciare un nemico attivo sulla mappa.
     *
     * @param model    Il modello fisico dell'entità nemica.
     * @param enemyId  L'identificativo univoco del tipo di nemico.
     */
    private record ActiveEnemy(AbstractPhysicalEntityModel model, String enemyId) { }

    /**
     * Record interno che mappa la configurazione di una singola ondata caricata.
     *
     * @param threatLevel   Il livello di minaccia associato all'ondata.
     * @param totalEnemies  Il numero totale di nemici previsti per l'ondata.
     */
    private record WaveConfig(ThreatLevel threatLevel, int totalEnemies) { }

    /** Proprietà globale osservabile per il conteggio totale dei nemici sconfitti. */
    private static final IntegerProperty totalKillsProperty    = new SimpleIntegerProperty(0);

    /** Proprietà osservabile per il numero di nemici attualmente rimasti vivi nell'ondata. */
    private final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);

    /** Proprietà osservabile per il numero totale di nemici previsti nell'ondata corrente. */
    private final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);

    /** Proprietà osservabile per l'indice numerico dell'ondata corrente. */
    private final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);

    /** Proprietà osservabile per la rappresentazione testuale del livello di minaccia attuale. */
    private final StringProperty currentThreatLevelProperty = new SimpleStringProperty("LOW");

    /** @return La proprietà osservabile delle uccisioni totali. */
    public static IntegerProperty totalKillsProperty()     { return totalKillsProperty; }

    /** @return La proprietà osservabile dei nemici rimanenti nell'ondata attuale. */
    public IntegerProperty enemiesRemainingProperty()      { return enemiesRemainingProperty; }

    /** @return La proprietà osservabile del totale di nemici dell'ondata attuale. */
    public IntegerProperty waveTotalProperty()             { return waveTotalProperty; }

    /** @return La proprietà osservabile dell'indice dell'ondata corrente. */
    public IntegerProperty currentWaveProperty()           { return currentWaveProperty; }

    /** @return La proprietà osservabile del livello di minaccia corrente. */
    public StringProperty currentThreatLevelProperty()     { return currentThreatLevelProperty; }

    /** @return Il numero intero di nemici rimanenti nell'ondata attuale. */
    public int getEnemiesRemaining()   { return enemiesRemainingProperty.get(); }

    /** @return Il numero intero totale di nemici previsti per l'ondata attuale. */
    public int getWaveTotal()          { return waveTotalProperty.get(); }

    /**
     * Incrementa il contatore globale delle uccisioni se l'entità specificata
     * possiede un record valido e un livello di minaccia diverso da NONE.
     *
     * @param entity L'entità fisica che è stata eliminata.
     */
    public static void incrementKills(AbstractPhysicalEntityModel entity) {
        if (entity == null) return;
        EntityRecord record = entity.getEntityRecord();
        if (record == null || record.threatLevel() == null || record.threatLevel() == ThreatLevel.NONE) return;
        totalKillsProperty.set(totalKillsProperty.get() + 1);
    }

    /** Lista dei nemici attualmente attivi e vivi all'interno dell'universo di gioco. */
    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();

    /** Lista delle configurazioni delle ondate caricate dal file risorsa JSON. */
    private final List<WaveConfig> fileWaves = new ArrayList<>();

    /** Callback di fine partita eseguito allo scadere del timer di vittoria dell'ultima ondata. */
    private Runnable onBossDeadCallback;

    /** Generatore di numeri casuali per angoli e distanze di spawn dei nemici. */
    private final Random random       = new Random();

    /** Flag che indica se il boss principale dell'universo è stato generato. */
    private boolean bossSpawned       = false;

    /** Flag che indica se il boss principale è stato sconfitto. */
    private boolean bossDead          = false;

    /** Flag che indica se i requisiti di vittoria dell'ultima ondata sono stati soddisfatti. */
    private boolean gameWon                  = false;

    /** Timer di countdown per ritardare l'attivazione del cambio scena dopo la vittoria. */
    private double victoryTimer              = 0.0;

    /** Flag di sicurezza per evitare l'esecuzione multipla del callback di vittoria. */
    private boolean victoryCallbackTriggered = false;

    /** Indice numerico dell'ondata corrente. */
    private int currentWave                  = 1;

    /** Livello di minaccia associato all'ondata correntemente attiva. */
    private ThreatLevel currentThreatLevel   = ThreatLevel.LOW;

    /** Quantità complessiva di nemici che devono essere generati nell'ondata corrente. */
    private int totalEnemiesToSpawnThisWave  = 0;

    /** Quantità di nemici già generati nell'ondata corrente. */
    private int enemiesSpawnedThisWave       = 0;

    /** Flag che indica se il gioco si trova nella fase di pausa tra un'ondata e la successiva. */
    private boolean inIntermission    = true;

    /** Timer rimanente per la fine dell'intermissione corrente. */
    private double intermissionTimer = 5.0;

    /** Timer di cooldown tra la generazione di un singolo nemico e il successivo. */
    private double  spawnTimer        = 0.0;

    /** Intervallo di tempo standard espresso in secondi tra lo spawn di ciascun nemico. */
    private static final double SPAWN_DELAY           = 1.5;

    /** Durata standard espressa in secondi dell'intervallo di pausa tra le ondate. */
    private static final double INTERMISSION_DURATION = 5.0;

    /** Durata iniziale espressa in secondi dell'intermissione prima dell'avvio della prima ondata. */
    private static final double FIRST_WAVE_DELAY      = 5.0;

    /** Modello del bestiario per verificare lo stato di sblocco delle entità nemiche. */
    private final BestiaryModel bestiaryModel = new BestiaryModel();

    /**
     * Esegue il caricamento e il parsing manuale sequenziale del file risorsa JSON
     * contenente la sequenza delle ondate e dei livelli di minaccia personalizzati.
     *
     * @param resourcePath Il percorso della risorsa JSON all'interno del pacchetto applicativo.
     */
    public void loadWavesFromResource(String resourcePath) {
        fileWaves.clear();
        URL url = getClass().getResource(resourcePath);

        if (url == null) {
            System.err.println("[WAVE CONTROLLER] Risorsa non trovata: " + resourcePath + ". Fallback procedurale.");
            return;
        }

        // Read the entire file into a String (using try-with-resources)
        StringBuilder sb = new StringBuilder();
        try (InputStream is = url.openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.err.println("[WAVE CONTROLLER] Errore nella lettura del file: " + resourcePath);
            return;
        }

        // Parse the JSON array
        try {
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String threatStr = obj.getString("threat_level");
                int total = obj.getInt("total_enemies");

                ThreatLevel level = ThreatLevel.valueOf(threatStr.toUpperCase().trim());
                fileWaves.add(new WaveConfig(level, total));
            }
        } catch (Exception e) {
            System.err.println("[WAVE CONTROLLER] Errore nel parsing del JSON delle ondate: " + e.getMessage());
            return;
        }

        System.out.printf("[WAVE CONTROLLER] JSON caricato! Rilevate %d ondate custom.%n", fileWaves.size());

        // If we are about to start the game (currentWave == 1) and we have waves, set initial threat level
        if (!fileWaves.isEmpty() && currentWave == 1) {
            currentThreatLevel = fileWaves.get(0).threatLevel();
            currentThreatLevelProperty.set(currentThreatLevel.name());
        }
    }

    /**
     * Estrae il valore testuale associato a una chiave specifica da una stringa oggetto JSON.
     *
     * @param objStr La porzione di stringa racchiusa tra parentesi graffe che rappresenta l'oggetto JSON.
     * @param key    La chiave di cui estrarre il rispettivo valore.
     * @return       Il valore estratto come stringa pulita, oppure null se la chiave non viene trovata.
     */
    private String extractJsonValue(String objStr, String key) {
        int keyIndex = objStr.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;
        int colonIndex = objStr.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int valueStart = colonIndex + 1;
        while (valueStart < objStr.length() && (Character.isWhitespace(objStr.charAt(valueStart)) || objStr.charAt(valueStart) == '"')) {
            valueStart++;
        }

        int valueEnd = valueStart;
        while (valueEnd < objStr.length() && !Character.isWhitespace(objStr.charAt(valueEnd)) && objStr.charAt(valueEnd) != '"' && objStr.charAt(valueEnd) != ',') {
            valueEnd++;
        }

        return (valueStart >= valueEnd) ? null : objStr.substring(valueStart, valueEnd);
    }

    /**
     * Aggiorna lo stato logico del controllore ad ogni frame di gioco.
     * Gestisce la rimozione dei nemici distrutti, il countdown dei timer di intermissione,
     * la temporizzazione dello spawn dei singoli nemici e il controllo del timer di vittoria.
     *
     * @param dt        Il tempo trascorso dall'ultimo frame espresso in secondi.
     * @param camera    Il modello della telecamera per calcolare le coordinate di spawn off-screen.
     * @param gameModel Il modello generale del gioco per verificare la validità dello stato del giocatore.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());
        enemiesRemainingProperty.set(activeEnemies.size());

        if (gameWon && !victoryCallbackTriggered) {
            victoryTimer -= dt;
            if (victoryTimer <= 0) {
                victoryCallbackTriggered = true;
                if (onBossDeadCallback != null) {
                    onBossDeadCallback.run();
                }
            }
            return;
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

    /**
     * Configura e avvia la logica di una nuova ondata.
     * Determina i parametri di minaccia e volume dei nemici leggendoli dalle liste caricate da JSON
     * oppure applica algoritmi di generazione procedurale qualora non vi siano dati custom sufficienti.
     */
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
            if (currentWave <= 2) currentThreatLevel = ThreatLevel.LOW;
            else if (currentWave <= 4) currentThreatLevel = ThreatLevel.NORMAL;
            else if (currentWave <= 6) currentThreatLevel = ThreatLevel.HIGH;
            else if (currentWave <= 9) currentThreatLevel = ThreatLevel.EXTREME;
            else currentThreatLevel = ThreatLevel.APOCALYPSE;

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

        currentThreatLevelProperty.set(currentThreatLevel.name());

        if (currentThreatLevel == ThreatLevel.APOCALYPSE) {
            System.out.println("[WAVE CONTROLLER] !!! APOCALYPSE DETECTED: IMMINENT BOSS SPAWN !!!");
        }

        AudioManager.getInstance().playSFX("alarm");

        waveTotalProperty.set(totalEnemiesToSpawnThisWave);
        enemiesRemainingProperty.set(totalEnemiesToSpawnThisWave);
        currentWaveProperty.set(currentWave);
    }

    /**
     * Conclude l'ondata attualmente attiva.
     * Verifica se l'ondata appena completata coincide con l'ultima del file di configurazione custom;
     * in caso affermativo imposta lo stato di vittoria del gioco, altrimenti avvia l'intermissione per l'ondata successiva.
     */
    private void endCurrentWave() {
        System.out.printf("[WAVE CONTROLLER] WAVE %d COMPLETATA!%n", currentWave);

        if (!fileWaves.isEmpty() && currentWave >= fileWaves.size()) {
            System.out.println("[WAVE CONTROLLER] !!! COMPLIMENTI: ULTIMA ONDATA SUPERATA. GIOCO VINTO !!!");
            gameWon = true;
            victoryTimer = 3.0;
            victoryCallbackTriggered = false;

            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
            return;
        }

        inIntermission    = true;
        intermissionTimer = INTERMISSION_DURATION;
        currentWave++;

        int nextWaveIndex = currentWave - 1;
        if (!fileWaves.isEmpty() && nextWaveIndex < fileWaves.size()) {
            currentThreatLevelProperty.set(fileWaves.get(nextWaveIndex).threatLevel().name());
        }
    }

    /**
     * Calcola una posizione casuale lungo una circonferenza esterna rispetto al raggio di visione
     * della telecamera ed esegue lo spawn di un singolo nemico idoneo tramite l'UniverseSpawner.
     *
     * @param camera    La telecamera usata come centro del calcolo della distanza di spawn.
     * @param gameModel Il modello globale per l'iniezione dell'entità generata nel sistema.
     */
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

    /**
     * Filtra e seleziona un identificativo di nemico idoneo dal registro di fabbrica delle entità,
     * basandosi sul livello di minaccia corrente dell'ondata e sullo stato dei progressi sbloccati nel bestiario dell'utente.
     *
     * @return L'identificativo testuale del nemico estratto casualmente dal pool idoneo.
     */
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

    /**
     * Attiva lo stato del boss nel sistema di gioco avviando la traccia musicale di sottofondo dedicata.
     */
    public void notifyBossSpawned() {
        if (!bossSpawned) {
            bossSpawned = true;
            bossDead    = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
        }
    }

    /**
     * Registra l'avvenuta sconfitta del boss, reimpostando la musica di sottofondo standard.
     * La gestione della vittoria finale rimane demandata esclusivamente al completamento totale dei nemici dell'ondata.
     */
    public void notifyBossDead() {
        if (bossSpawned && !bossDead) {
            bossDead = true;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
        }
    }

    /**
     * Ripristina integralmente lo stato interno del controllore riportandolo ai parametri di default iniziali.
     * Azzera tutti i contatori di ondate, nemici vivi, uccisioni globali e i timer di vittoria,
     * preparando l'istanza per una nuova partita pulita.
     */
    public void reset() {
        spawnTimer                  = 0.0;
        bossSpawned                 = false;
        bossDead                    = false;

        gameWon                     = false;
        victoryTimer                = 0.0;
        victoryCallbackTriggered    = false;

        currentWave                 = 1;

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

    /**
     * Imposta la routine esterna da lanciare nel momento in cui il timer post-vittoria scade con successo.
     *
     * @param callback L'oggetto {@link Runnable} che definisce l'azione di fine partita.
     */
    public void setOnBossDeadCallback(Runnable callback) { this.onBossDeadCallback = callback; }

    /** @return L'indice intero dell'ondata corrente. */
    public int         getCurrentWave()        { return currentWave; }
}