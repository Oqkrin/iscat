package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.interfaces.UsersQueriesInterface;
import uni.gaben.iscat.iscat_screens.login.model.User;
import uni.gaben.iscat.utils.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class SqliteUsersQueries implements UsersQueriesInterface {

    private final Connection conn;

    public SqliteUsersQueries() {
        this.conn = IscatDB.getInstance().getConnection();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT ID, Username, Password, DateOfCreation, LastLogin
                FROM Utenti
                WHERE Username = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = """
                SELECT ID, Username, Password, DateOfCreation, LastLogin
                FROM Utenti
                WHERE ID = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String username) {
        String sql = """
                SELECT 1
                FROM Utenti
                WHERE Username = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User create(String username, String rawPassword) {
        String insertUserSql = """
                INSERT INTO Utenti(
                    Username,
                    Password,
                    DateOfCreation,
                    LastLogin
                )
                VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        String insertSettingsSql = """
                INSERT INTO ImpostazioniUtenti (UserID)
                VALUES (?)
                """;

        String hashedPassword = PasswordHasher.hash(rawPassword);

        try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {

            userStmt.setString(1, username);
            userStmt.setString(2, hashedPassword);
            userStmt.executeUpdate();

            try (ResultSet rs = userStmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to retrieve generated user ID.");
                }

                int generatedId = rs.getInt(1);

                // CREAZIONE AUTOMATICA DELLE IMPOSTAZIONI UTENTE
                try (PreparedStatement settingsStmt = conn.prepareStatement(insertSettingsSql)) {
                    settingsStmt.setInt(1, generatedId);
                    settingsStmt.executeUpdate();
                }

                return findById(generatedId).orElseThrow();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateLastLogin(int userId, LocalDateTime loginTime) {
        String sql = """
                UPDATE Utenti
                SET LastLogin = ?
                WHERE ID = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(loginTime));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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