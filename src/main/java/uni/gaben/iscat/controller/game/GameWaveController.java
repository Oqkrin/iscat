package uni.gaben.iscat.controller.game;

import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.model.game.GameModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controllore dei flussi e delle ondate di nemici all'interno dell'universo di gioco.
 * Coordina il ciclo vitale di spawn procedurale fuori schermo, calcola dinamicamente la progressione
 * della difficoltà basandosi sui tempi di gioco e sul livello del giocatore, e gestisce
 * l'algoritmo di estrazione probabilistica pesata (RNG) per la selezione delle tipologie di nemici.
 */
public class GameWaveController {

    /**
     * Struttura dati interna per la mappatura e il tracciamento accoppiato
     * di un'istanza fisica di gioco e della sua chiave identificativa sul database.
     */
    private static class ActiveEnemy {
        final AbstractEntityModel model;
        final String enemyId;

        ActiveEnemy(AbstractEntityModel model, String enemyId) {
            this.model = model;
            this.enemyId = enemyId;
        }
    }

    /** Contatore statico cumulativo delle uccisioni totali eseguite nella sessione di gioco corrente. */
    public static int totalKills = 0;

    /** Incrementa di un'unità il contatore globale dei nemici abbattuti. */
    public static void incrementKills() { totalKills++; }

    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();

    // Flag di stato per l'attivazione dei tier dei nemici basati sulle soglie di uccisioni
    private boolean unlockedEater = false;
    private boolean unlockedFake = false;
    private boolean unlockedBomber = false;
    private boolean unlockedDasher = false;
    private boolean unlockedHealer = false;
    private boolean unlockedGolem = false;
    private boolean unlockedCore = false;
    private boolean unlockedWorm = false;
    private boolean unlockedMother = false;
    private boolean unlockedMaster = false;

    /** Se impostato a true, forza l'attivazione immediata del Boss finale al prossimo ciclo utile. */
    public boolean forceBossSpawn = false;

    private double spawnTimer = 0.0;
    private final Random random = new Random();

    private boolean bossSpawned = false;
    private boolean bossDead = false;

    private static final double INITIAL_SPAWN_COOLDOWN = 6.0;

    /**
     * Aggiorna lo stato temporale della gestione ondate, rimuove le entità distrutte e
     * valuta le condizioni di time-threshold e spawn-rate per l'iniezione di nuovi mob nel mondo.
     *
     * @param dt        Delta time (tempo trascorso dall'ultimo tick).
     * @param camera    Modello geometrico della telecamera per il calcolo delle zone di esclusione visiva.
     * @param gameModel Riferimento al modello macroscopico dello stato del gioco.
     */
    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null || gameModel.getUniverseModel().getPlayer() == null) return;

        // Pulizia automatica e rimozione dal tracciamento dei corpi fisici distrutti
        activeEnemies.removeIf(ae -> ae.model == null || ae.model.shouldRemove());

        checkUnlockStates();

        double masterTimeSec = gameModel.getTotalElapsedSeconds();
        int maxAllowedGlobal = getMaxEnemiesByTime(masterTimeSec);

        // Se le entità attive sono inferiori al tetto massimo per questa fascia temporale, forza lo spawn immediato
        if (activeEnemies.size() < maxAllowedGlobal) {
            spawnTimer = 0.0;
        } else {
            spawnTimer -= dt;
        }

        if (spawnTimer <= 0) {
            boolean spawnedAtLeastOne = spawnDynamicWaveOffScreen(
                    camera,
                    masterTimeSec,
                    gameModel.getUniverseModel().getPlayer().getLevel(),
                    maxAllowedGlobal
            );

            if (spawnedAtLeastOne) {
                // Il tempo di cooldown tra le ondate si riduce gradualmente all'aumentare delle kill totali dell'utente
                spawnTimer = Math.max(2.0, INITIAL_SPAWN_COOLDOWN - (totalKills / 50.0));
            } else {
                // Se i limiti per tipologia bloccano la generazione, esegue un ri-controllo rapido al frame successivo
                spawnTimer = 0.2;
            }
        }
    }

    /**
     * Genera un blocco procedurali di avversari dislocandoli lungo i margini esterni
     * del frustum visibile della telecamera di gioco, scalandone i parametri vitali di conseguenza.
     */
    private boolean spawnDynamicWaveOffScreen(CameraModel camera, double masterTimeSec, int playerLevel, int maxAllowedGlobal) {
        if (camera == null) return false;

        int totalEnemiesToSpawn = 3 + (totalKills / 15);
        int spawnedInThisBatch = 0;
        boolean successfullySpawned = false;

        while (activeEnemies.size() < maxAllowedGlobal && spawnedInThisBatch < totalEnemiesToSpawn) {
            String enemyIdToSpawn = rollEnemyId();

            // Verifica del raggiungimento dei tetti massimi (Soft Cap) per la tipologia estratta
            if (!canSpawnEnemyType(enemyIdToSpawn)) {
                if (!"iscat_mob".equals(enemyIdToSpawn) && canSpawnEnemyType("iscat_mob")) {
                    enemyIdToSpawn = "iscat_mob"; // Ripiego sul mob base in caso di saturazione di unità speciali
                } else {
                    break;
                }
            }

            // Calcolo geometrico dei quattro quadranti esterni al viewport visibile
            double margin = 80.0;
            double halfWidth = (camera.getScreenWidth() / 2.0) + margin;
            double halfHeight = (camera.getScreenHeight() / 2.0) + margin;

            int side = random.nextInt(4);
            double spawnX = camera.getX();
            double spawnY = camera.getY();

            switch (side) {
                case 0 -> { // Quadrante Superiore
                    spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                    spawnY = camera.getY() - halfHeight;
                }
                case 1 -> { // Quadrante Inferiore
                    spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                    spawnY = camera.getY() + halfHeight;
                }
                case 2 -> { // Quadrante Sinistro
                    spawnX = camera.getX() - halfWidth;
                    spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
                }
                case 3 -> { // Quadrante Destro
                    spawnX = camera.getX() + halfWidth;
                    spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
                }
            }

            // Iniezione nel motore tramite il Factory Spawner centralizzato
            Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyIdToSpawn, spawnX, spawnY);

            if (spawnedObject instanceof AbstractEntityModel enemyModel) {
                // Algoritmo di scaling lineare della salute del mob basato su livello del giocatore e tempo di sopravvivenza
                if (enemyModel instanceof LivingEntityModel livingModel) {
                    int difficultyLevel = (int) (playerLevel + masterTimeSec / 60);
                    livingModel.setMaxLife(livingModel.getMaxLife() * difficultyLevel);
                    livingModel.setLife(livingModel.getMaxLife());
                }

                activeEnemies.add(new ActiveEnemy(enemyModel, enemyIdToSpawn));
                spawnedInThisBatch++;
                successfullySpawned = true;

                if ("ISCAT_MASTER".equals(enemyIdToSpawn) && !bossSpawned) {
                    notifyBossSpawned();
                }
            } else {
                break;
            }
        }

        return successfullySpawned;
    }

    /**
     * Verifica la conformità dell'entità con le soglie di sovraffollamento locali e globali per tipo.
     */
    private boolean canSpawnEnemyType(String enemyId) {
        if ("ISCAT_MASTER".equals(enemyId)) {
            return !bossSpawned;
        }

        long count = activeEnemies.stream().filter(ae -> enemyId.equals(ae.enemyId)).count();

        if ("WORM".equals(enemyId)) return count < 5;
        return count < 10;
    }

    /**
     * Ritorna la densità massima consentita di mob attivi in mappa contemporaneamente in base al tempo di gioco.
     */
    private int getMaxEnemiesByTime(double timeSec) {
        if (timeSec >= 210.0) return 20; // 3:30 minuti
        if (timeSec >= 150.0) return 15; // 2:30 minuti
        if (timeSec >= 60.0)  return 10; // 1:00 minuto
        if (timeSec >= 30.0)  return 5;  // 30 secondi
        return 3;
    }

    /**
     * Monitora le soglie di avanzamento delle uccisioni storiche per abilitare internamente
     * le flag logiche di idoneità allo spawn delle rispettive categorie di nemici.
     */
    private void checkUnlockStates() {
        if (totalKills >= 5 && !unlockedEater) unlockedEater = true;
        if (totalKills >= 15 && !unlockedFake) unlockedFake = true;
        if (totalKills >= 25 && !unlockedBomber) unlockedBomber = true;
        if (totalKills >= 40 && !unlockedDasher) unlockedDasher = true;
        if (totalKills >= 60 && !unlockedHealer) unlockedHealer = true;
        if (totalKills >= 80 && !unlockedGolem) unlockedGolem = true;
        if (totalKills >= 100 && !unlockedCore) unlockedCore = true;
        if (totalKills >= 120 && !unlockedWorm) unlockedWorm = true;
        if (totalKills >= 150 && !unlockedMother) unlockedMother = true;
        if (totalKills >= 200 && !unlockedMaster) unlockedMaster = true;
    }

    /**
     * Esegue un'estrazione probabilistica (RHE - Roulette Wheel Selection) calcolando dinamicamente
     * i pesi di rilevanza di ciascun nemico. I pesi dei nemici base decrementano col tempo,
     * lasciando spazio di penetrazione statistica alle entità d'élite man mano che aumentano i punti kill.
     *
     * @return La chiave stringa normalizzata identificativa dell'entità da spawnare.
     */
    private String rollEnemyId() {
        if (!bossSpawned && (forceBossSpawn || totalKills >= 200)) {
            return "ISCAT_MASTER";
        }

        // Calcolo dinamico e progressivo delle curve di probabilità (Weights)
        double mobWeight    = Math.max(10.0, 100.0 - (totalKills * 0.5));
        double eaterWeight  = (totalKills >= 5)   ? Math.min(40.0, (totalKills - 5) * 1.5) : 0.0;
        double fakeWeight   = (totalKills >= 15)  ? Math.min(35.0, (totalKills - 15) * 1.5) : 0.0;
        double bomberWeight = (totalKills >= 25)  ? Math.min(30.0, (totalKills - 25) * 1.5) : 0.0;
        double dasherWeight = (totalKills >= 40)  ? Math.min(30.0, (totalKills - 40) * 1.5) : 0.0;
        double healerWeight = (totalKills >= 60)  ? Math.min(25.0, (totalKills - 60) * 1.5) : 0.0;
        double golemWeight  = (totalKills >= 80)  ? Math.min(30.0, (totalKills - 80) * 1.5) : 0.0;
        double coreWeight   = (totalKills >= 100) ? Math.min(30.0, (totalKills - 100) * 1.5) : 0.0;
        double wormWeight   = (totalKills >= 120) ? Math.min(25.0, (totalKills - 120) * 1.5) : 0.0;
        double motherWeight = (totalKills >= 150) ? Math.min(25.0, (totalKills - 150) * 1.5) : 0.0;

        double totalWeightSum = mobWeight + eaterWeight + fakeWeight + bomberWeight + dasherWeight
                + healerWeight + golemWeight + coreWeight + wormWeight + motherWeight;

        double rollValue = random.nextDouble() * totalWeightSum;
        double currentSum = 0.0;

        if ((currentSum += mobWeight) >= rollValue)    return "iscat_mob";
        if ((currentSum += eaterWeight) >= rollValue)  return "eater";
        if ((currentSum += fakeWeight) >= rollValue)   return "fake_iscat";
        if ((currentSum += bomberWeight) >= rollValue) return "iscat_bomber";
        if ((currentSum += dasherWeight) >= rollValue) return "iscat_dasher";
        if ((currentSum += healerWeight) >= rollValue) return "ISCAT_HEALER";
        if ((currentSum += golemWeight) >= rollValue)  return "fallen_star_golem";
        if ((currentSum += coreWeight) >= rollValue)   return "iscat_core";
        if ((currentSum += wormWeight) >= rollValue)   return "WORM";
        if ((currentSum += motherWeight) >= rollValue) return "iscat_mother";

        return "iscat_mob";
    }

    /**
     * Notifica la discesa in campo del Boss finale, aggiornando le tracce e mutando la
     * traccia audio di sottofondo (BGM) sul tema di combattimento del Boss.
     */
    public void notifyBossSpawned() {
        if (!bossSpawned) {
            bossSpawned = true;
            bossDead = false;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/boss.wav", true);
        }
    }

    /**
     * Riceve la notifica del definitivo abbattimento del Boss finale, interrompendo lo stato di allerta
     * e ripristinando il tema d'esplorazione e vittoria standard nell'universo.
     */
    public void notifyBossDead() {
        if (bossSpawned && !bossDead) {
            bossDead = true;
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        }
    }

    /**
     * Ripristina integralmente i parametri operativi del gestore delle ondate, svuotando le liste
     * di tracciamento e azzerando i contatori transitori in preparazione di una nuova sessione di gioco.
     */
    public void reset() {
        this.spawnTimer = 0.0;
        this.bossSpawned = false;
        this.bossDead = false;
        totalKills = 0;
        activeEnemies.clear();
        unlockedEater = false;
        unlockedFake = false;
        unlockedBomber = false;
        unlockedDasher = false;
        unlockedHealer = false;
        unlockedGolem = false;
        unlockedCore = false;
        unlockedWorm = false;
        unlockedMother = false;
        unlockedMaster = false;
    }
}
