package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.UserDAO;
import uni.gaben.iscat.model.user.User;
import uni.gaben.iscat.utils.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementazione SQLite del DAO per la gestione dell'anagrafica e dell'autenticazione utenti.
 * Gestisce l'hashing delle password in fase di creazione e il parsing sicuro delle date.
 */
public class SQLiteUserDAO implements UserDAO {

    public SQLiteUserDAO() {
    }

    /** Cerca un utente tramite il suo username (case-sensitive). */
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

    /** Verifica se l'username esiste già, ignorando la differenza tra maiuscole e minuscole. */
    @Override
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM Utenti WHERE LOWER(Username) = LOWER(?)";
        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica di esistenza di: " + username, e);
        }
    }

    /** Cripta la password tramite {@link PasswordHasher} e registra il nuovo utente generando l'ID. */
    @Override
    public User create(String username, String rawPassword) {
        String sql = """
                INSERT INTO Utenti (Username, Password, DateOfCreation, LastLogin)
                VALUES (?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        String hashedPassword = PasswordHasher.hash(rawPassword);

        try (Connection connection = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new User(id, username, hashedPassword, now, now);
                } else {
                    throw new SQLException("Creazione utente fallita, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione dell'utente: " + username, e);
        }
    }

    /** Aggiorna il timestamp relativo all'ultimo accesso effettuato dall'utente. */
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

    /** Modifica l'username dell'utente nel database. */
    @Override
    public void updateUsername(int userId, String newUsername) {
        String sql = "UPDATE Utenti SET Username = ? WHERE ID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento username per userId: " + userId, e);
        }
    }

    /** Aggiorna la password dell'utente (riceve l'hash già calcolato). */
    @Override
    public void updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE Utenti SET Password = ? WHERE ID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento password per userId: " + userId, e);
        }
    }

    /** Mappa una riga del ResultSet in un oggetto di tipo User. */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("ID"),
                rs.getString("Username"),
                rs.getString("Password"),
                safeParseLocalDateTime(rs, "DateOfCreation"),
                safeParseLocalDateTime(rs, "LastLogin")
        );
    }

    /** Gestisce il parsing delle date sia in formato stringa standard ISO/SQLite sia in timestamp numerico (epoch millis). */
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