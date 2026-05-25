package uni.gaben.iscat.iscat_game.universe;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_game.camera.CameraModel;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyWaveController {

    // Record interno di supporto per tracciare il tipo associato a ciascuna istanza generata
    private static class ActiveEnemy {
        Object entity;
        UniverseSpawnable type;

        ActiveEnemy(Object entity, UniverseSpawnable type) {
            this.entity = entity;
            this.type = type;
        }
    }

    // STATISTICHE GLOBALI
    public static int totalKills = 0;
    public static void incrementKills() { totalKills++; }

    // TRACCIAMENTO ISTANZE ATTIVE
    private final List<ActiveEnemy> activeEnemies = new ArrayList<>();

    // FLAG PER GESTIONE DELLE NOTIFICHE DI SBLOCCO
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

    public boolean forceBossSpawn = false;

    private double spawnTimer = 0.0;
    private final Random random = new Random();

    private boolean bossSpawned = false;
    private boolean bossDead = false;

    private static final double INITIAL_SPAWN_COOLDOWN = 6.0;

    public void update(double dt, CameraModel camera, GameModel gameModel) {
        if (gameModel == null) return;

        // Rimuove istantaneamente i nemici distrutti dal motore grafico
        activeEnemies.removeIf(ae -> {
            if (ae.entity instanceof AbstractEntityModel entityModel) {
                return entityModel.shouldRemove();
            }
            return true;
        });

        // CONTROLLO E PRINT DEI NUOVI SBLOCCHI DI NEMICI
        checkUnlockPrints();

        double masterTimeSec = gameModel.getTotalElapsedSeconds();
        int maxAllowedGlobal = getMaxEnemiesByTime(masterTimeSec);

        // Se siamo sotto la soglia massima consentita dal tempo, azzeriamo il timer per forzare lo spawn
        if (activeEnemies.size() < maxAllowedGlobal) {
            spawnTimer = 0.0;
        } else {
            spawnTimer -= dt;
        }

        if (spawnTimer <= 0) {
            boolean spawnedAtLeastOne = spawnDynamicWaveOffScreen(camera, masterTimeSec, maxAllowedGlobal);

            if (spawnedAtLeastOne) {
                // Calcola il prossimo cooldown standard (si riduce man mano che aumentano le kill)
                spawnTimer = Math.max(2.0, INITIAL_SPAWN_COOLDOWN - (totalKills / 50.0));
            } else {
                // Se i caps o i limiti specifici bloccano lo spawn, ricontrolla quasi subito
                spawnTimer = 0.2;
            }
        }
    }

    /**
     * Ciclo di spawn dinamico ottimizzato con controllo While per evitare stalli
     */
    private boolean spawnDynamicWaveOffScreen(CameraModel camera, double currentSessionTime, int maxAllowedGlobal) {
        if (camera == null) return false;

        int totalEnemiesToSpawn = 3 + (totalKills / 15);
        int spawnedInThisBatch = 0;
        boolean successfullySpawned = false;

        // Il ciclo continua a girare finché c'è spazio nel cap globale e non abbiamo esaurito il batch della wave
        while (activeEnemies.size() < maxAllowedGlobal && spawnedInThisBatch < totalEnemiesToSpawn) {
            UniverseSpawnable enemyToSpawn = rollWeightProbability();

            // Controllo dei tetti massimi specifici richiesti
            if (!canSpawnEnemyType(enemyToSpawn)) {
                // Se un nemico speciale ha raggiunto il suo tetto massimo, ripiega sul mob base (se ha spazio)
                if (enemyToSpawn != UniverseSpawnable.ISCAT_MOB && canSpawnEnemyType(UniverseSpawnable.ISCAT_MOB)) {
                    enemyToSpawn = UniverseSpawnable.ISCAT_MOB;
                } else {
                    // Se anche il mob base è saturo, interrompiamo il riempimento per questo frame
                    break;
                }
            }

            // CALCOLO DELLE COORDINATE DI SPAWN
            // margine di sicurezza oltre il bordo dello schermo
            double margin = 80.0;

            // Calcolo confini della camera
            double halfWidth = (camera.getScreenWidth() / 2.0) + margin;
            double halfHeight = (camera.getScreenHeight() / 2.0) + margin;

            // Scegliamo random una side
            int side = random.nextInt(4);
            double spawnX = camera.getX();
            double spawnY = camera.getY();

            switch (side) {
                case 0: // SOPRA
                    spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                    spawnY = camera.getY() - halfHeight;
                    break;
                case 1: // SOTTO
                    spawnX = camera.getX() - halfWidth + (random.nextDouble() * halfWidth * 2);
                    spawnY = camera.getY() + halfHeight;
                    break;
                case 2: // SINISTRA
                    spawnX = camera.getX() - halfWidth;
                    spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
                    break;
                case 3: // DESTRA
                    spawnX = camera.getX() + halfWidth;
                    spawnY = camera.getY() - halfHeight + (random.nextDouble() * halfHeight * 2);
                    break;
            }

            Object spawnedObject = UniverseSpawner.getInstance().spawn(enemyToSpawn, spawnX, spawnY);

            if (spawnedObject != null) {
                activeEnemies.add(new ActiveEnemy(spawnedObject, enemyToSpawn));
                spawnedInThisBatch++;
                successfullySpawned = true;

                //System.out.println("[Spawn] Generato nemico: " + enemyToSpawn + " a coordinate (" + (int)spawnX + ", " + (int)spawnY + ")");

                if (enemyToSpawn == UniverseSpawnable.ISCAT_MASTER && !bossSpawned) {
                    notifyBossSpawned();
                }
            } else {
                break;
            }
        }

        return successfullySpawned;
    }

    /**
     * Verifica e applica i tetti massimi specifici per ogni tipologia di nemico
     */
    private boolean canSpawnEnemyType(UniverseSpawnable type) {
        if (type == UniverseSpawnable.ISCAT_MASTER) {
            return !bossSpawned; // Il Master può essere al massimo 1 alla volta
            //TODO il Master facciamo che respawna in endless mode magari
        }

        // Filtra e conta quanti nemici di questo tipo specifico sono registrati e vivi nell'ondata
        long count = activeEnemies.stream().filter(ae -> ae.type == type).count();

        return switch (type) {
            case WORM -> count < 5;          // Iscatworm: max 5
            case ISCAT_MOTHER -> count < 2;  // Iscat Mother: max 2
            default -> count < 10;           // Tutti gli altri nemici standard: max 10
        };
    }

    /**
     * Ritorna il tetto massimo globale di nemici attivi contemporaneamente in base al tempo
     */
    private int getMaxEnemiesByTime(double timeSec) {
        if (timeSec >= 210.0) return 20; // 3:30 min -> max 20
        if (timeSec >= 150.0) return 15; // 2:30 min -> max 15
        if (timeSec >= 60.0)  return 10; // 1:00 min -> max 10
        if (timeSec >= 30.0)  return 5;  // 30 secondi -> max 5
        return 3;                        // Inizio partita (0-30s) -> max 3
    }

    /**
     * Stampa in console lo sblocco del nemico non appena si superano le soglie di kill
     */
    private void checkUnlockPrints() {
        if (totalKills >= 5 && !unlockedEater) { unlockedEater = true; System.out.println("[EnemyWave] Ora EATER può spawnare!"); }
        if (totalKills >= 15 && !unlockedFake) { unlockedFake = true; System.out.println("[EnemyWave] Ora FAKE_ISCAT può spawnare!"); }
        if (totalKills >= 25 && !unlockedBomber) { unlockedBomber = true; System.out.println("[EnemyWave] Ora ISCAT_BOMBER può spawnare!"); }
        if (totalKills >= 40 && !unlockedDasher) { unlockedDasher = true; System.out.println("[EnemyWave] Ora ISCAT_DASHER può spawnare!"); }
        if (totalKills >= 60 && !unlockedHealer) { unlockedHealer = true; System.out.println("[EnemyWave] Ora ISCAT_HEALER può spawnare!"); }
        if (totalKills >= 80 && !unlockedGolem) { unlockedGolem = true; System.out.println("[EnemyWave] Ora FALLEN_STAR_GOLEM può spawnare!"); }
        if (totalKills >= 100 && !unlockedCore) { unlockedCore = true; System.out.println("[EnemyWave] Ora ISCAT_CORE può spawnare!"); }
        if (totalKills >= 120 && !unlockedWorm) { unlockedWorm = true; System.out.println("[EnemyWave] Ora WORM può spawnare!"); }
        if (totalKills >= 150 && !unlockedMother) { unlockedMother = true; System.out.println("[EnemyWave] Ora ISCAT_MOTHER può spawnare!"); }
        if (totalKills >= 200 && !unlockedMaster) { unlockedMaster = true; System.out.println("[EnemyWave] Ora ISCAT_MASTER può spawnare!"); }
    }

    private UniverseSpawnable rollWeightProbability() {
        if (!bossSpawned && (forceBossSpawn || totalKills >= 200)) {
            return UniverseSpawnable.ISCAT_MASTER;
        }

        double mobWeight = Math.max(10.0, 100.0 - (totalKills * 0.5));
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

        if ((currentSum += mobWeight) >= rollValue)    return UniverseSpawnable.ISCAT_MOB;
        if ((currentSum += eaterWeight) >= rollValue)  return UniverseSpawnable.EATER;
        if ((currentSum += fakeWeight) >= rollValue)   return UniverseSpawnable.FAKE_ISCAT;
        if ((currentSum += bomberWeight) >= rollValue) return UniverseSpawnable.ISCAT_BOMBER;
        if ((currentSum += dasherWeight) >= rollValue) return UniverseSpawnable.ISCAT_DASHER;
        if ((currentSum += healerWeight) >= rollValue) return UniverseSpawnable.ISCAT_HEALER;
        if ((currentSum += golemWeight) >= rollValue)  return UniverseSpawnable.FALLEN_STAR_GOLEM;
        if ((currentSum += coreWeight) >= rollValue)   return UniverseSpawnable.ISCAT_CORE;
        if ((currentSum += wormWeight) >= rollValue)   return UniverseSpawnable.WORM;
        if ((currentSum += motherWeight) >= rollValue) return UniverseSpawnable.ISCAT_MOTHER;

        return UniverseSpawnable.ISCAT_MOB;
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
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        }
    }

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