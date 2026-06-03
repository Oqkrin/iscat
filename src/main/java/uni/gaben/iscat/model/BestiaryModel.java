package uni.gaben.iscat.model;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.universe.entity.enemies.generic.GenericEntitySettings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce il caricamento e l'estrazione dei dati del Bestiario dal database SQLite.
 * Utilizza direttamente {@link GenericEntitySettings} come fonte unica di verità per i dati delle entità,
 * eliminando la ridondanza dei record personalizzati e mantenendo la coerenza con il resto del sistema.
 */
public class BestiaryModel {

    /**
     * Registro di sblocco: mappa il numero di uccisioni registrate indicizzandole
     * per la chiave normalizzata del nemico.
     */
    private final Map<String, Integer> killCounts = new LinkedHashMap<>();

    /**
     * Carica l'elenco completo dei nemici dal database insieme ai progressi dell'utente.
     * Utilizza il DAO standardizzato per garantire coerenza con il resto del sistema.
     *
     * @param userId Identificativo numerico dell'utente per il recupero delle metriche personali.
     * @return Una mappa ordinata che associa la chiave normalizzata del nemico al suo {@link GenericEntitySettings}.
     */
    public Map<String, GenericEntitySettings> loadEnemies(int userId) {
        Map<String, GenericEntitySettings> enemies = new LinkedHashMap<>();
        killCounts.clear();

        try {
            EnemyDAO enemyDAO = IscatDB.getInstance().getEnemyDAO();
            
            // Load all enemies from database (standardized DAO)
            List<GenericEntitySettings> allEnemies = enemyDAO.findAll();
            
            // Load bestiary entries with kill counts for this user
            List<EnemyDAO.BestiarioEntry> bestiaryEntries = enemyDAO.getBestiarioForUser(userId);
            
            // Build kill count map from bestiary entries
            Map<String, Integer> killCountMap = new LinkedHashMap<>();
            for (EnemyDAO.BestiarioEntry entry : bestiaryEntries) {
                // Match by name since BestiarioEntry doesn't have entityKey
                killCountMap.put(entry.name(), entry.killCount());
            }
            
            // Populate enemies map with settings and kill counts
            for (GenericEntitySettings settings : allEnemies) {
                String cleanKey = settings.entityKey.toLowerCase().trim();
                enemies.put(cleanKey, settings);
                
                // Match kill count by name
                Integer kills = killCountMap.getOrDefault(settings.name, 0);
                killCounts.put(cleanKey, kills);
            }
            
        } catch (Exception e) {
            System.err.println("[ERRORE CRITICO DB] Impossibile caricare l'elenco dei record per il Bestiario.");
            e.printStackTrace();
        }

        return enemies;
    }

    /**
     * Ritorna la mappa contenente il numero cumulativo di uccisioni
     * registrate per ciascuna chiave nemico.
     */
    public Map<String, Integer> getKillCounts() {
        return killCounts;
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
}