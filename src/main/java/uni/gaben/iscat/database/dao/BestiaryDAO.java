package uni.gaben.iscat.database.dao;

import java.util.Map;

public interface BestiaryDAO {
    /**
     * Incrementa il numero di uccisioni per un determinato nemico e utente.
     */
    void incrementKill(int userId, String enemyKey, int count);

    /**
     * Recupera l'intera mappa delle uccisioni registrate per un determinato utente.
     * @return Una mappa che associa l'EnemyKEY (in minuscolo) al numero di uccisioni.
     */
    Map<String, Integer> getKillsForUser(int userId);
}