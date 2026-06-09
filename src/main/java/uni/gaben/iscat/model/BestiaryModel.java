package uni.gaben.iscat.model;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.entity.GenericEntityFactory;
import uni.gaben.iscat.universe.entity.GenericEntitySettings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestisce l'estrazione dei dati del Bestiario unendo le definizioni statiche del file JSON
 * al registro delle uccisioni dell'utente memorizzato nella tabella "Bestiario" su SQLite.
 */
public class BestiaryModel {

    /**
     * Registro di sblocco in tempo reale: mappa il numero di uccisioni registrate
     * indicizzandole per la chiave normalizzata del nemico (es. "iscat_mob" -> 14).
     */
    private final Map<String, Integer> killCounts = new LinkedHashMap<>();

    /**
     * Carica l'elenco completo dei nemici dalla cache JSON insieme ai progressi dell'utente dal DB.
     *
     * @param userId Identificativo numerico dell'utente per il recupero delle metriche personali.
     * @return Una mappa ordinata che associa la chiave normalizzata del nemico al suo {@link GenericEntitySettings}.
     */
    public Map<String, GenericEntitySettings> loadEnemies(int userId) {
        Map<String, GenericEntitySettings> enemies = new LinkedHashMap<>();
        killCounts.clear();

        // Recuperiamo le definizioni statiche precaricate dal file JSON
        Map<String, GenericEntitySettings> jsonCache = GenericEntityFactory.getCache();

        if (jsonCache == null || jsonCache.isEmpty()) {
            System.err.println("[BESTIARIO] Attenzione: la cache dei nemici JSON è vuota o non inizializzata!");
            return enemies;
        }

        // Inerroghiamo il BestiaryDAO per ottenere la mappa delle uccisioni salvate su DB (EnemyKEY -> KilledTimes)
        Map<String, Integer> dbKills = IscatDB.getInstance().getBestiaryDAO().getKillsForUser(userId);

        // Uniamo i dati: mappiamo ogni nemico del JSON associando il rispettivo counter delle uccisioni
        for (Map.Entry<String, GenericEntitySettings> entry : jsonCache.entrySet()) {
            String cleanKey = entry.getKey();

            // Popoliamo la mappa per la View del Bestiario
            enemies.put(cleanKey, entry.getValue());

            // Estraiamo il counter reale dal database o impostiamo 0 se il mostro non è mai stato ucciso
            int totalKills = dbKills.getOrDefault(cleanKey, 0);
            killCounts.put(cleanKey, totalKills);
        }

        return enemies;
    }

    /**
     * Verifica lo stato di sblocco di un determinato nemico all'interno del Bestiario.
     * Un'entità torna ad essere considerata sbloccata SOLO se il contatore delle uccisioni
     * estratto dal DB è strettamente maggiore di zero.
     *
     * @param entityKey La stringa identificativa del nemico da ispezionare.
     * @return true se il giocatore ha sconfitto l'entità almeno una volta, false altrimenti.
     */
    public boolean isUnlocked(String entityKey) {
        if (entityKey == null) return false;
        Integer kills = killCounts.get(entityKey.toLowerCase().trim());
        return kills != null && kills > 0;
    }

    /**
     * Ottiene il numero di uccisioni per un nemico specifico.
     */
    public int getKillCount(String entityKey) {
        if (entityKey == null) return 0;
        return killCounts.getOrDefault(entityKey.toLowerCase().trim(), 0);
    }

    /**
     * Ritorna la mappa contenente il numero cumulativo di uccisioni
     * registrate per ciascuna chiave nemico.
     */
    public Map<String, Integer> getKillCounts() {
        return killCounts;
    }
}