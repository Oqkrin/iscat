package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.scores.SaveData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteScoreDAO implements ScoreDAO {

    public SQLiteScoreDAO() {
    }

    @Override
    public void createIfNotExists(int userId) {
        String sql = "INSERT OR IGNORE INTO Salvataggi (UserID) VALUES (?)";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore createIfNotExists per userId: " + userId, e);
        }
    }

    @Override
    public Optional<SaveData> load(int userId) {
        String sql = "SELECT * FROM Salvataggi WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new SaveData(
                            rs.getInt("UserID"),
                            rs.getInt("Score"),
                            rs.getInt("Deaths"),
                            rs.getInt("TotalDamageDealt"),
                            rs.getInt("TotalDamageReceived"),
                            rs.getInt("BestTime")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il caricamento del salvataggio per userId: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(int userId, String column, int value) {
        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Nome colonna statistica non valido: " + column);
        }

        String sql = "UPDATE Salvataggi SET " + column + " = ? WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della colonna " + column + " per userId: " + userId, e);
        }
    }

    @Override
    public void increment(int userId, String column, int amount) {
        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Nome colonna statistica non valido: " + column);
        }

        String sql = "UPDATE Salvataggi SET " + column + " = " + column + " + ? WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'incremento della colonna " + column + " per userId: " + userId, e);
        }
    }

    @Override
    public void reset(int userId) {
        String sql = """
            UPDATE Salvataggi 
            SET Score = 0, Deaths = 0, TotalDamageDealt = 0, TotalDamageReceived = 0, BestTime = 0 
            WHERE UserID = ?
            """;
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore reset statistiche per userId: " + userId, e);
        }
    }

    @Override
    public List<UserScoreEntry> getAllScores() {
        List<UserScoreEntry> scores = new ArrayList<>();
        String sql = """
            SELECT u.Username, s.Score
            FROM Utenti u
            INNER JOIN Salvataggi s ON u.ID = s.UserID
            ORDER BY s.Score DESC
            """;

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("Username");
                int score = rs.getInt("Score");
                scores.add(new UserScoreEntry(username, score));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel caricamento della leaderboard globale", e);
        }
        return scores;
    }

    private boolean isValidColumn(String column) {
        return column != null && column.matches("(?i)Score|Deaths|TotalDamageDealt|TotalDamageReceived|BestTime");
    }
}