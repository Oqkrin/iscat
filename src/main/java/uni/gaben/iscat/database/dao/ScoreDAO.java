package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.screens.scores.SaveData;

import java.util.List;
import java.util.Optional;

public interface ScoreDAO {

    /** Crea un record in Salvataggi se non esiste già per questo utente */
    void createIfNotExists(int userId);

    /** Carica i dati di salvataggio dell'utente */
    Optional<SaveData> load(int userId);

    /** Aggiorna un singolo campo numerico */
    void update(int userId, String column, int value);

    /** Incrementa un campo numerico del valore dato */
    void increment(int userId, String column, int amount);

    /** Azzera tutte le statistiche di salvataggio per l'utente specificato */
    void reset(int userId);

    /** Ottiene tutti gli score di tutti gli utenti per la leaderboard */
    List<UserScoreEntry> getAllScores();

    record UserScoreEntry(String username, int score) {}
}