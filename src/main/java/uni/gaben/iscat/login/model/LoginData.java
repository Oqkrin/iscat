package uni.gaben.iscat.login.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Store utenti con password hashate SHA-256 + salt.
 * Formato stored: "salt:hash" (entrambi Base64).
 * Con un DB in futuro basta sostituire la Map con query JDBC/JPA.
 */
public class LoginData {

    private final Map<String, String> utentiRegistrati;
    private final SecureRandom rng = new SecureRandom();

    /** @param utentiIniziali mappa username → password in chiaro (solo per bootstrap/dummy data) */
    public LoginData(Map<String, String> utentiIniziali) {
        this.utentiRegistrati = new HashMap<>();
        utentiIniziali.forEach((u, p) -> utentiRegistrati.put(u, hash(p)));
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
