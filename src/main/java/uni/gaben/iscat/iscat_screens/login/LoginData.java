package uni.gaben.iscat.iscat_screens.login;

import uni.gaben.iscat.IscatDB;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * User store with SHA-256 + salt hashed passwords.
 * Stored format: "salt:hash" (both Base64).
 * Swap the Map for JDBC/JPA queries when a real DB is introduced.
 */
public class LoginData {

    private final Map<String, String> utentiRegistrati;
    private final SecureRandom rng = new SecureRandom();

    /**
     * Creates a LoginData pre-populated with the given plaintext credentials.
     * Only use for bootstrap/dummy data — passwords are hashed on construction.
     */
    public LoginData(Map<String, String> utentiIniziali) {
        this.utentiRegistrati = new HashMap<>();
        utentiIniziali.forEach((u, p) -> utentiRegistrati.put(u, hash(p)));
    }

    /**
     * Factory for the default development seed account.
     * Keeps hardcoded credentials out of the Application class.
     */
    public static LoginData withPlaceholder() {
        return new LoginData(Map.of("gaben", "iscat"));
    }

    /**
     * Checks if a user already exists in the database.
     */
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM Utenti WHERE Username = ?";

        // FIX: Remove 'conn' from the resource declaration block so the global connection stays open
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extracts the stored salt to verify the password attempt.
     */
    public boolean checkPassword(String username, String password) {
        String sql = "SELECT Password FROM Utenti WHERE Username = ?";

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;

                String storedValue = rs.getString("Password"); // Fetches "salt:hash"

                // FIX: Parse the existing salt out of the database string
                String[] parts = storedValue.split(":");
                if (parts.length != 2) return false;

                String storedSaltBase64 = parts[0];
                String storedHash = parts[1];

                // Re-hash the incoming password using the parsed salt
                byte[] salt = Base64.getDecoder().decode(storedSaltBase64);
                String calculatedHash = hashWithSalt(password, salt);

                return storedHash.equals(calculatedHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registers a new user with a freshly generated salt and password hash.
     */
    public void register(String username, String password) {
        String sql = "INSERT INTO Utenti(Username, Password, DateOfCreation) VALUES (?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, hash(password)); // Uses a fresh random salt
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    // createDefaultSettings(userId);
                    // createDefaultSave(userId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hash(String password) {
        byte[] salt = new byte[16];
        rng.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hashWithSalt(password, salt);
    }

    private String hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
