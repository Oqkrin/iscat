package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.model.ScoreModel;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione e la persistenza dei punteggi e dello score utente.
 */
public interface ScoreDAO {

    /** Crea un record iniziale per le statistiche dell'utente se non esiste. */
    void createIfNotExists(int userId);

    /** Carica le statistiche e i punteggi completi di un utente. */
    Optional<ScoreModel> load(int userId);

    /** Sovrascrive il valore di una colonna specifica. */
    void update(int userId, String column, int value);

    /** Incrementa una colonna specifica del valore indicato. */
    void increment(int userId, String column, int amount);

    /** Ripristina ai valori di default tutte le statistiche di un utente. */
    void reset(int userId);

    /** Recupera i punteggi globali di tutti gli utenti ordinati per la classifica. */
    List<UserScoreEntry> getAllScores();

    /** record immutabile per rappresentare una riga della classifica. */
    record UserScoreEntry(String username, int score, int totalKills, int bestTime) {}
}