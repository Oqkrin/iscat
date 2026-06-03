package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.model.ScoreModel;

import java.util.List;
import java.util.Optional;

public interface ScoreDAO {

    /** Crea un record in UserScore se non esiste già per questo utente */
    void createIfNotExists(int userId);

    /** Carica i dati statistiche dell'utente */
    Optional<ScoreModel> load(int userId);

    /** Aggiorna un singolo campo numerico */
    void update(int userId, String column, int value);

    /** Incrementa un campo numerico del valore dato */
    void increment(int userId, String column, int amount);

    /** Azzera tutte le statistiche per l'utente specificato */
    void reset(int userId);

    /** Ottiene tutti gli score di tutti gli utenti per la leaderboard */
    List<UserScoreEntry> getAllScores();

    /** Ottiene i top N punteggi */
    List<UserScoreEntry> getTopScores(int limit);

    record UserScoreEntry(String username, int score, int totalKills, int bestTime) {}
}