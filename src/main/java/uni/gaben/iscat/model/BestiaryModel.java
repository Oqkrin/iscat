package uni.gaben.iscat.model;

import uni.gaben.iscat.universe.entity.GenericEntityFactory;
import uni.gaben.iscat.universe.entity.GenericEntitySettings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestisce l'estrazione dei dati del Bestiario sfruttando esclusivamente il file JSON.
 * Utilizza la cache di {@link GenericEntityFactory} come unica fonte di verità per i dati delle entità,
 * sbloccando immediatamente l'intero catalogo biologico dei nemici.
 */
public class BestiaryModel {

    /**
     * Carica l'elenco completo dei nemici direttamente dalla cache del sistema JSON.
     * Non effettua chiamate al database, garantendo performance istantanee.
     *
     * @param userId Inutilizzato (mantenuto solo per non rompere le firme dei controller esistenti).
     * @return Una mappa ordinata che associa la chiave normalizzata del nemico al suo {@link GenericEntitySettings}.
     */
    public Map<String, GenericEntitySettings> loadEnemies(int userId) {
        // Creiamo una nuova mappa pulita per la View del Bestiario
        Map<String, GenericEntitySettings> enemies = new LinkedHashMap<>();

        // Recuperiamo tutte le definizioni statiche precaricate dal file JSON
        Map<String, GenericEntitySettings> jsonCache = GenericEntityFactory.getCache();

        if (jsonCache != null) {
            // Copiamo i riferimenti nella mappa ordinata di output
            enemies.putAll(jsonCache);
        } else {
            System.err.println("[BESTIARIO] Attenzione: la cache dei nemici JSON è vuota o non inizializzata!");
        }

        return enemies;
    }

    /**
     * Verifica lo stato di sblocco di un determinato nemico all'interno del Bestiario.
     * Senza un database a tenere traccia delle uccisioni, ogni nemico valido presente nel JSON
     * viene considerato sbloccato di default (true).
     *
     * @param entityKey La stringa identificativa del nemico da ispezionare.
     * @return true se il nemico esiste nella cache JSON, false altrimenti.
     */
    public boolean isUnlocked(String entityKey) {
        if (entityKey == null) return false;
        String cleanKey = entityKey.toLowerCase().trim();
        return GenericEntityFactory.getCache().containsKey(cleanKey);
    }

    /**
     * Ottiene il numero di uccisioni per un nemico specifico.
     * Senza database, restituisce costantemente 0.
     */
    public int getKillCount(String entityKey) {
        return 0;
    }

    /**
     * Ritorna una mappa vuota o fittizia per non spaccare eventuali chiamate esterne della View.
     */
    public Map<String, Integer> getKillCounts() {
        Map<String, Integer> dummyMap = new LinkedHashMap<>();
        for (String key : GenericEntityFactory.getCache().keySet()) {
            dummyMap.put(key, 0);
        }
        return dummyMap;
    }
}