package uni.gaben.iscat.database.dao;

import java.util.Map;

/**
 * DAO per la gestione e il tracciamento delle uccisioni dei nemici (Bestiario) per ciascun utente.
 */
public interface BestiaryDAO {

    /** Incrementa il contatore delle uccisioni di un determinato nemico per l'utente specificato. */
    void incrementKill(int userId, String enemyKey, int count);

    /** * Recupera l'elenco completo delle uccisioni dell'utente.
     * @return Una mappa che associa l'identificativo del nemico (in minuscolo) al numero di uccisioni totali.
     */
    Map<String, Integer> getKillsForUser(int userId);
}