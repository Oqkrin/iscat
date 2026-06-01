package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.UserDAO;
import uni.gaben.iscat.screens.login.model.User;
import uni.gaben.iscat.utils.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class SQLiteUserDAO implements UserDAO {

    public SQLiteUserDAO() {
        // Nessuna memorizzazione di connessioni a lungo termine per evitare handle obsoleti
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT ID, Username, Password, DateOfCreation, LastLogin
                FROM Utenti
                WHERE Username = ?
                """;

        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante findByUsername: " + username, e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = """
                SELECT ID, Username, Password, DateOfCreation, LastLogin
                FROM Utenti
                WHERE ID = ?
                """;

        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante findById: " + id, e);
        }
    }

    @Override
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM Utenti WHERE LOWER(Username) = LOWER(?)";

        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica esistenza utente: " + username, e);
        }
    }

    @Override
    public User create(String username, String rawPassword) {
        String sql = "INSERT INTO Utenti (Username, Password, DateOfCreation) VALUES (?, ?, ?)";
        String hashedPassword = PasswordHasher.hash(rawPassword);
        LocalDateTime now = LocalDateTime.now();

        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword);
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new User(id, username, hashedPassword, now, null);
                } else {
                    throw new SQLException("Creazione utente fallita, nessun ID autogenerato ottenuto.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la registrazione utente: " + username, e);
        }
    }

    @Override
    public void updateLastLogin(int userId, LocalDateTime loginTime) {
        String sql = """
                UPDATE Utenti
                SET LastLogin = ?
                WHERE ID = ?
                """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(loginTime));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento dell'ultimo login per userId: " + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("ID"),
                rs.getString("Username"),
                rs.getString("Password"),
                safeParseLocalDateTime(rs, "DateOfCreation"),
                safeParseLocalDateTime(rs, "LastLogin")
        );
    }

    private LocalDateTime safeParseLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.matches("\\d+")) {
            long epochMillis = Long.parseLong(value);
            return Timestamp.from(java.time.Instant.ofEpochMilli(epochMillis)).toLocalDateTime();
        }

        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}