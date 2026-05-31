package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) dedicato alla gestione della persistenza e dell'estrazione
 * delle entità nemiche e dei dati storici del Bestiario dell'utente.
 * Segue il pattern di disconnessione atomica: espone metodi statici thread-safe, interroga il singleton
 * {@link IscatDB} e adotta la sintassi try-with-resources per garantire la chiusura immediata delle risorse JDBC.
 */
public class EnemyDAO {

    /** Costruttore privato per impedire l'istanziazione di questa classe utility. */
    private EnemyDAO() {}

    /**
     * Contenitore dati leggero (DTO) adibito al trasferimento delle informazioni
     * necessarie per popolare gli elementi visivi della schermata di selezione del Bestiario.
     *
     * @param name        Nome visibile del nemico (oscurato in "???" se bloccato).
     * @param description Testo di lore o istruzioni di sblocco.
     * @param spritePath  Percorso del file grafico dello spritesheet.
     * @param killCount   Numero cumulativo di uccisioni registrate dall'utente.
     * @param isUnlocked  True se l'utente ha abbattuto l'entità almeno una volta.
     */
    public record BestiarioEntry(String name, String description, String spritePath, int killCount, boolean isUnlocked) {}

    /**
     * Incrementa in modo persistente il contatore delle uccisioni per un determinato utente e nemico.
     * Applica un algoritmo atomico di UPSERT basato sul vincolo di chiave composta della tabella 'BestiarioUtente'.
     *
     * @param userId    Identificativo numerico dell'utente in sessione.
     * @param entityKey Stringa identificativa univoca del nemico (es. "iscat_mob", "iscat_master").
     */
    public static void incrementKill(int userId, String entityKey) {
        if (entityKey == null) return;
        String normalizedKey = entityKey.toLowerCase().trim();

        String sql = """
            INSERT INTO BestiarioUtente (UserID, EnemyID, KillCount)
            VALUES (
                ?, 
                (SELECT ID FROM Entita WHERE LOWER(EntityKey) = ?), 
                1
            )
            ON CONFLICT(UserID, EnemyID) DO UPDATE SET 
                KillCount = KillCount + 1;
        """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, normalizedKey);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[EnemyDAO] Errore nell'incremento delle kill per il Bestiario (Key: " + normalizedKey + ")");
            e.printStackTrace();
        }
    }

    /**
     * Estrae la lista completa dei nemici censiti unendoli con i dati d'avanzamento dell'utente specificato.
     * Qualora l'entità non risulti ancora abbattuta dall'utente, provvede ad applicare dinamicamente
     * stringhe di mascheramento ("???") a tutela delle meccaniche di spoiler del gameplay.
     *
     * @param userId Identificativo numerico dell'utente di cui ispezionare il progresso.
     * @return Una lista di DTO {@link BestiarioEntry} ordinata in base all'ID sequenziale del database.
     */
    public static List<BestiarioEntry> getBestiarioForUser(int userId) {
        List<BestiarioEntry> list = new ArrayList<>();

        String sql = """
            SELECT e.Name, e.Description, e.SpritePath, COALESCE(b.KillCount, 0) AS Kills
            FROM Entita e
            LEFT JOIN BestiarioUtente b ON e.ID = b.EnemyID AND b.UserID = ?
            ORDER BY e.ID ASC;
        """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int kills = rs.getInt("Kills");
                    boolean unlocked = kills > 0;

                    String name = unlocked ? rs.getString("Name") : "???";
                    String desc = unlocked ? rs.getString("Description") : "Sconfiggi questo nemico nell'universo per sbloccare i suoi dati.";
                    String sprite = rs.getString("SpritePath");

                    list.add(new BestiarioEntry(name, desc, sprite, kills, unlocked));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EnemyDAO] Errore nel caricamento del bestiario per l'utente: " + userId);
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Interroga la tabella delle entità per estrarre la configurazione strutturale e biologica
     * di un singolo nemico associato alla chiave stringa passata come argomento.
     *
     * @param entityKey Chiave stringa identificativa dell'entità da cercare.
     * @return Un'istanza {@link Optional} contenente il DTO compilato, oppure un Optional vuoto se non trovato.
     */
    public static Optional<GenericEntitySettings> findByKey(String entityKey) {
        if (entityKey == null) return Optional.empty();
        String normalizedKey = entityKey.toLowerCase().trim();

        String sql = "SELECT * FROM Entita WHERE LOWER(TRIM(EntityKey)) = ?";

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normalizedKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EnemyDAO] Eccezione SQL durante la ricerca selettiva di: " + normalizedKey);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Estrae e restituisce l'intera anagrafica dei nemici archiviati all'interno della tabella 'Entita'.
     * Utilizzato principalmente dal motore di precaricamento della fabbrica polimorfica (Factory).
     *
     * @return Una lista contenente tutti i DTO di configurazione censiti nel database.
     */
    public static List<GenericEntitySettings> findAll() {
        String sql = "SELECT * FROM Entita";
        List<GenericEntitySettings> results = new ArrayList<>();

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EnemyDAO] Errore nell'estrazione massiva (findAll) delle entità");
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Converte la riga corrente di un ResultSet nell'oggetto di configurazione tipizzato.
     * Mappa tutti i parametri fisici, comportamentali e grafici richiesti dall'engine di gioco.
     */
    private static GenericEntitySettings mapRow(ResultSet rs) throws SQLException {
        GenericEntitySettings s = new GenericEntitySettings();

        s.entityKey   = rs.getString("EntityKey");
        s.name        = rs.getString("Name");
        s.description = rs.getString("Description");

        s.spritePath  = rs.getString("SpritePath");
        s.frameW      = rs.getInt("FrameW");
        s.frameH      = rs.getInt("FrameH");

        s.initLife        = rs.getDouble("InitLife");
        s.dimSprite       = rs.getInt("FrameW"); // Sincronizzato con la larghezza nativa del frame
        s.scale           = rs.getDouble("Scale");
        s.dampingLineare  = rs.getDouble("LinearDamping");
        s.maxVelocity     = rs.getDouble("MaxVelocity");
        s.force           = rs.getDouble("Force");
        s.rotationSpeed   = rs.getDouble("RotationSpeed");
        s.xpReward        = rs.getInt("XPReward");
        s.detectionRange  = rs.getDouble("DetectionRange");
        s.combatRange     = rs.getDouble("CombatRange");
        s.preferredRange  = rs.getDouble("PreferredRange");
        s.fireCooldownS   = rs.getDouble("FireCooldownS");
        s.customParam1    = rs.getDouble("CustomParam1");
        s.customParam2    = rs.getDouble("CustomParam2");

        s.shapeType    = GenericEntitySettings.ShapeType.fromString(rs.getString("ShapeType"));
        s.behaviorType = GenericEntitySettings.BehaviorType.fromString(rs.getString("BehaviorType"));

        return s;
    }
}