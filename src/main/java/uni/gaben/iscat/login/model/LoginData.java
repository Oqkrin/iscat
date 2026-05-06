package uni.gaben.iscat.login.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
    public static LoginData withDefaults() {
        return new LoginData(Map.of("gaben", "iscat"));
    }

    public boolean exists(String username) {
        return utentiRegistrati.containsKey(username);
    }

    public boolean checkPassword(String username, String password) {
        String stored = utentiRegistrati.get(username);
        if (stored == null) return false;
        String[] parts = stored.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        return parts[1].equals(hashWithSalt(password, salt));
    }

    public void register(String username, String password) {
        utentiRegistrati.put(username, hash(password));
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
