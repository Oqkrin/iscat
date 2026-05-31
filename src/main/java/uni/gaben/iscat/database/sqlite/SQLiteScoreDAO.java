package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.scores.SaveData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SQLiteScoreDAO implements ScoreDAO {


    public SQLiteScoreDAO() {}

    // Helper method per ottenere una connessione fresca ogni volta
    private Connection getConnection() throws SQLException {
        return IscatDB.getInstance().getConnection();
    }

    @Override
    public void createIfNotExists(int userId) {
        String sql = "INSERT OR IGNORE INTO Salvataggi (UserID) VALUES (?)";
        try (Connection conn = getConnection();  // Connessione locale
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore createIfNotExists per userId: " + userId, e);
        }
    }

    @Override
    public Optional<SaveData> load(int userId) {
        String sql = "SELECT * FROM Salvataggi WHERE UserID = ?";
        try (Connection conn = getConnection();  // Connessione locale
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            throw new RuntimeException("Errore load per userId: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(int userId, String column, int value) {
        if (!isValidColumn(column)) return;

        String sql = "UPDATE Salvataggi SET " + column + " = ? WHERE UserID = ?";
        try (Connection conn = getConnection();  // Connessione locale
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore update per userId: " + userId, e);
        }
    }

    @Override
    public void increment(int userId, String column, int amount) {
        if (!isValidColumnForIncrement(column)) return;

        String sql = "UPDATE Salvataggi SET " + column + " = " + column + " + ? WHERE UserID = ?";
        try (Connection conn = getConnection();  // Connessione locale
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore increment per userId: " + userId, e);
        }
    }

    @Override
    public void reset(int userId) {
        String sql = "UPDATE Salvataggi SET Score = 0, Deaths = 0, TotalDamageDealt = 0, " +
                "TotalDamageReceived = 0, BestTime = 0 WHERE UserID = ?";
        try (Connection conn = getConnection();  // Connessione locale
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore reset per userId: " + userId, e);
        }
    }

    private boolean isValidColumn(String column) {
        return column.matches("(?i)Score|Deaths|TotalDamageDealt|TotalDamageReceived|BestTime");
    }

    private boolean isValidColumnForIncrement(String column) {
        return column.matches("(?i)Score|Deaths|TotalDamageDealt|TotalDamageReceived");
    }
}