package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.ScoreModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione SQLite del DAO per la gestione dello score.
 * Salva i record dei giocatori sulla tabella 'UserScore' e gestisce le transazioni di reset.
 */
public class SQLiteScoreDAO implements ScoreDAO {

    public SQLiteScoreDAO() {
    }

    /** Inserisce un record vuoto per l'utente se non è già presente nella tabella. */
    @Override
    public void createIfNotExists(int userId) {
        String sql = "INSERT OR IGNORE INTO UserScore (UserID) VALUES (?)";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore createIfNotExists per userId: " + userId, e);
        }
    }

    /** Carica tutte le statistiche di un utente mappandole nel rispettivo modello. */
    @Override
    public Optional<ScoreModel> load(int userId) {
        String sql = "SELECT * FROM UserScore WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ScoreModel(
                            rs.getInt("UserID"),
                            rs.getInt("Score"),
                            rs.getInt("TotalKills"),
                            rs.getInt("Deaths"),
                            rs.getInt("TotalDamageDealt"),
                            rs.getInt("TotalDamageReceived"),
                            rs.getInt("BestTime"),
                            rs.getInt("BoostCollected"),
                            rs.getInt("LongestTime"),
                            rs.getInt("TimesPlayed"),
                            rs.getInt("TimesLogged")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il caricamento dello score per userId: " + userId, e);
        }
        return Optional.empty();
    }

    /** Aggiorna il valore di una colonna specifica previa validazione tramite whitelist. */
    @Override
    public void update(int userId, String column, int value) {
        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Nome colonna statistica non valido: " + column);
        }

        String sql = "UPDATE UserScore SET " + column + " = ?, LastUpdated = CURRENT_TIMESTAMP WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della colonna " + column + " per userId: " + userId, e);
        }
    }

    /** Incrementa il valore di una colonna specifica salvaguardando la query da SQL Injection. */
    @Override
    public void increment(int userId, String column, int amount) {
        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Nome colonna statistica non valido: " + column);
        }

        String sql = "UPDATE UserScore SET " + column + " = " + column + " + ?, LastUpdated = CURRENT_TIMESTAMP WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'incremento della colonna " + column + " per userId: " + userId, e);
        }
    }

    /** * Ripristina a zero le statistiche e svuota il bestiario dell'utente in una singola transazione ACID.
     */
    @Override
    public void reset(int userId) {
        String updateScoreSql = """
        UPDATE UserScore 
        SET Score = 0, 
            TotalKills = 0, 
            Deaths = 0, 
            TotalDamageDealt = 0, 
            TotalDamageReceived = 0, 
            BestTime = 0,
            BoostCollected = 0,
            LongestTime = 0,
            TimesPlayed = 0,
            TimesLogged = 0,
            LastUpdated = CURRENT_TIMESTAMP
        WHERE UserID = ?
        """;

        String deleteBestiarySql = "DELETE FROM Bestiario WHERE UserID = ?";

        try (Connection conn = IscatDB.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmtScore = conn.prepareStatement(updateScoreSql)) {
                    stmtScore.setInt(1, userId);
                    stmtScore.executeUpdate();
                }

                try (PreparedStatement stmtBestiary = conn.prepareStatement(deleteBestiarySql)) {
                    stmtBestiary.setInt(1, userId);
                    stmtBestiary.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il reset delle statistiche e del bestiario per userId: " + userId, e);
        }
    }

    /** Recupera l'elenco completo di tutti i punteggi combinando i dati con la tabella utenti. */
    @Override
    public List<UserScoreEntry> getAllScores() {
        List<UserScoreEntry> scores = new ArrayList<>();
        String sql = """
            SELECT u.Username, s.Score, s.TotalKills, s.BestTime
            FROM Utenti u
            INNER JOIN UserScore s ON u.ID = s.UserID
            ORDER BY s.Score DESC
            """;

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("Username");
                int score = rs.getInt("Score");
                int totalKills = rs.getInt("TotalKills");
                int bestTime = rs.getInt("BestTime");
                scores.add(new UserScoreEntry(username, score, totalKills, bestTime));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel caricamento della leaderboard globale", e);
        }
        return scores;
    }

    /** Recupera i migliori punteggi limitando i risultati al valore specificato. */
    @Override
    public List<UserScoreEntry> getTopScores(int limit) {
        List<UserScoreEntry> scores = new ArrayList<>();
        String sql = """
            SELECT u.Username, s.Score, s.TotalKills, s.BestTime
            FROM Utenti u
            INNER JOIN UserScore s ON u.ID = s.UserID
            ORDER BY s.Score DESC
            LIMIT ?
            """;

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("Username");
                    int score = rs.getInt("Score");
                    int totalKills = rs.getInt("TotalKills");
                    int bestTime = rs.getInt("BestTime");
                    scores.add(new UserScoreEntry(username, score, totalKills, bestTime));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel caricamento della top " + limit + " punteggi", e);
        }
        return scores;
    }

    /** Controlla la validità dei nomi delle colonne tramite espressione regolare per evitare attacchi injection. */
    private boolean isValidColumn(String column) {
        return column != null && column.matches("(?i)Score|TotalKills|Deaths|TotalDamageDealt|TotalDamageReceived|BestTime|BoostCollected|LongestTime|TimesPlayed|TimesLogged");
    }
}