package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.scores.SaveData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScoreDAO {

    private ScoreDAO() {}

    /** Crea un record in Salvataggi se non esiste già per questo utente */
    public static void createIfNotExists(int userId) {
        String sql = "INSERT OR IGNORE INTO Salvataggi (UserID) VALUES (?)";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Carica i dati di salvataggio dell'utente */
    public static SaveData load(int userId) {
        String sql = "SELECT * FROM Salvataggi WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new SaveData(
                            rs.getInt("UserID"),
                            rs.getInt("Score"),
                            rs.getInt("Deaths"),
                            rs.getInt("TotalDamageDealt"),
                            rs.getInt("TotalDamageReceived"),
                            rs.getInt("BestTime")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SaveData(userId, 0, 0, 0, 0, 0);
    }

    /** Aggiorna un singolo campo numerico */
    public static void update(int userId, String column, int value) {
        if (!column.matches("(?i)Score|Deaths|TotalDamageDealt|TotalDamageReceived|BestTime")) return;
        String sql = "UPDATE Salvataggi SET " + column + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Incrementa un campo numerico del valore dato */
    public static void increment(int userId, String column, int amount) {
        if (!column.matches("(?i)Score|Deaths|TotalDamageDealt|TotalDamageReceived")) return;
        String sql = "UPDATE Salvataggi SET " + column + " = " + column + " + ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Azzera tutte le statistiche di salvataggio per l'utente specificato */
    public static void reset(int userId) {
        String sql = "UPDATE Salvataggi SET Score = 0, Deaths = 0, TotalDamageDealt = 0, " +
                "TotalDamageReceived = 0, BestTime = 0 WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}