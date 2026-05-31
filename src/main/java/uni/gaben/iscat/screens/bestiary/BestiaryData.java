package uni.gaben.iscat.screens.bestiary;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestisce il caricamento, l'estrazione e l'astrazione dei dati del Bestiario dal database SQLite.
 * Implementa una strategia di disaccoppiamento dei dati: separa le proprietà biologiche, cinematiche
 * e strutturali immutabili delle entità di gioco (modellate dal record {@link Enemy}) dalle
 * statistiche di progresso dinamiche (contatore uccisioni) legate alla sessione dello specifico utente.
 */
public class BestiaryData {

    /**
     * Rappresenta i dati strutturali, biologici e di comportamento di un nemico.
     * Questo record modella esclusivamente le proprietà intrinseche e immutabili dell'entità
     * così come definite a livello di design nel database.
     *
     * @param entityKey      Chiave stringa univoca di censimento (es. "iscat_mob").
     * @param name           Nome visualizzato del nemico.
     * @param sprite         Percorso della risorsa grafica interna per lo spritesheet.
     * @param frameW         Larghezza in pixel di un singolo frame d'animazione.
     * @param frameH         Altezza in pixel di un singolo frame d'animazione.
     * @param scale          Fattore di scala dimensionale da applicare in fase di rendering.
     * @param linearDamping  Coefficiente di attrito lineare per il corpo rigido del motore fisico.
     * @param description    Testo descrittivo di lore ed analisi biologica.
     * @param initLife       Punti vita iniziali massimi dell'entità.
     * @param maxVelocity    Velocità limite massima raggiungibile dal corpo fisico (Terminal Velocity).
     * @param xpReward       Quantità di punti esperienza rilasciati al giocatore al momento del decesso.
     * @param detectionRange Raggio di attivazione e tracciamento della IA per l'ingaggio del bersaglio.
     * @param combatRange    Distanza ottimale per l'attivazione delle routine d'attacco e fuoco.
     * @param fireCooldownS  Tempo di ricarica espresso in secondi tra attacchi consecutivi.
     * @param behaviorType   Profilo comportamentale ("Brain Wiring") assegnato alla CPU.
     */
    public record Enemy(
            String entityKey,
            String name,
            String sprite,
            int frameW,
            int frameH,
            double scale,
            double linearDamping,
            String description,
            int initLife,
            int maxVelocity,
            int xpReward,
            int detectionRange,
            int combatRange,
            int fireCooldownS,
            String behaviorType
    ) {}

    /** * Registro di sblocco parallelo: mappa il numero di uccisioni registrate indicizzandole
     * per la chiave normalizzata del nemico. Impedisce la contaminazione del record immutabile
     * con stati transienti di sessione.
     */
    private final Map<String, Integer> extraKillCounts = new LinkedHashMap<>();

    /**
     * Esegue un'estrazione relazionale sul database per ricavare l'elenco globale dei nemici censiti,
     * effettuando un aggancio selettivo con i progressi di abbattimento dell'utente fornito.
     * Utilizza operatori di LEFT JOIN e funzioni di coalescenza per azzerare in modo sicuro i contatori
     * delle entità non ancora affrontate dal giocatore.
     *
     * @param userId Identificativo numerico dell'utente loggato per il recupero delle metriche personali.
     * @return Una mappa ordinata secondo l'indice sequenziale del database (LinkedHashMap) che associa
     * la chiave normalizzata del nemico al rispettivo oggetto {@link Enemy}.
     */
    public Map<String, Enemy> loadEnemies(int userId) {
        Map<String, Enemy> enemies = new LinkedHashMap<>();
        extraKillCounts.clear();

        String sql = """
            SELECT 
                e.EntityKey, e.Name, e.SpritePath, e.FrameW, e.FrameH, e.Scale, e.LinearDamping,
                e.Description, e.InitLife, e.MaxVelocity, e.XPReward, 
                e.DetectionRange, e.CombatRange, e.FireCooldownS, e.BehaviorType,
                COALESCE(b.KillCount, 0) AS RealKillCount
            FROM Entita e
            LEFT JOIN BestiarioUtente b ON e.ID = b.EnemyID AND b.UserID = ?
            ORDER BY e.ID ASC
        """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String entityKey   = rs.getString("EntityKey");
                    String cleanKey    = entityKey.toLowerCase().trim();

                    String name        = rs.getString("Name");
                    String sprite      = rs.getString("SpritePath");
                    int frameW         = rs.getInt("FrameW");
                    int frameH         = rs.getInt("FrameH");
                    double scale       = rs.getDouble("Scale");
                    double damping     = rs.getDouble("LinearDamping");
                    String desc        = rs.getString("Description");
                    int initLife       = rs.getInt("InitLife");
                    int maxVelocity    = rs.getInt("MaxVelocity");
                    int xpReward       = rs.getInt("XPReward");
                    int detectionRange = rs.getInt("DetectionRange");
                    int combatRange    = rs.getInt("CombatRange");
                    int fireCooldownS  = rs.getInt("FireCooldownS");
                    String behavior    = rs.getString("BehaviorType");

                    int killCount      = rs.getInt("RealKillCount");

                    Enemy enemy = new Enemy(
                            entityKey, name, sprite, frameW, frameH, scale, damping, desc,
                            initLife, maxVelocity, xpReward, detectionRange, combatRange, fireCooldownS,
                            behavior
                    );

                    enemies.put(cleanKey, enemy);
                    extraKillCounts.put(cleanKey, killCount);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERRORE CRITICO DB] Impossibile caricare l'elenco dei record per il Bestiario.");
            e.printStackTrace();
        }

        return enemies;
    }

    /**
     * Ritorna la mappa di associazione extra contenente il numero cumulativo di uccisioni
     * registrate per ciascuna chiave nemico.
     *
     * @return La mappa thread-safe o sequenziale dei contatori di kill storici.
     */
    public Map<String, Integer> getExtraKillCounts() {
        return extraKillCounts;
    }

    /**
     * Verifica lo stato di sblocco di un determinato nemico all'interno del Bestiario dell'utente.
     * Un'entità è considerata sbloccata se il contatore delle uccisioni associato è strettamente maggiore di zero.
     *
     * @param entityKey La stringa identificativa del nemico da ispezionare.
     * @return true se il giocatore ha sconfitto l'entità almeno una volta, false altrimenti.
     */
    public boolean isUnlocked(String entityKey) {
        if (entityKey == null) return false;
        Integer kills = extraKillCounts.get(entityKey.toLowerCase().trim());
        return kills != null && kills > 0;
    }
}