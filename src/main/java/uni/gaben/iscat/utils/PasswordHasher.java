package uni.gaben.iscat.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Componente di sicurezza crittografica per l'hashing e la verifica delle password.
 * Implementa l'algoritmo PBKDF2 (Password-Based Key Derivazione Function 2) con HMAC-SHA256,
 * allineato alle raccomandazioni di sicurezza OWASP correnti per contrastare attacchi brute-force e dizionario.
 */
public final class PasswordHasher {

    private static final SecureRandom RNG = new SecureRandom();

    private static final int SALT_LENGTH = 16; // Lunghezza del sale in byte (128 bit)

    // Valore ad alte prestazioni raccomandato da OWASP per massimizzare il costo computazionale del brute-force
    private static final int ITERATIONS = 600_000;

    private static final int KEY_LENGTH = 256; // Lunghezza della chiave derivata in bit

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordHasher() {
        /* Questa classe di utilità crittografica non deve essere istanziata */
    }

    /**
     * Genera un hash crittografico sicuro a partire da una password in chiaro.
     * Crea un sale casuale univoco per ogni computazione (evitando tabelle precalcolate/rainbow tables).
     *
     * @param rawPassword La password in chiaro da sottoporre a hashing.
     * @return Una stringa formattata nel formato {@code saleBase64:hashBase64}, pronta per il salvataggio nel database.
     * @throws RuntimeException In caso di anomalie dell'infrastruttura crittografica sottostante.
     */
    public static String hash(String rawPassword) {
        try {
            // Generazione del sale crittografico pseudo-casuale (CSPRNG)
            byte[] salt = new byte[SALT_LENGTH];
            RNG.nextBytes(salt);

            // Derivazione della chiave tramite PBKDF2
            byte[] hash = pbkdf2(rawPassword.toCharArray(), salt);

            // Restituisce i due blocchi concatenati da un separatore standard
            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Processo di hashing della password fallito", e);
        }
    }

    /**
     * Verifica la corrispondenza tra una password in chiaro e un hash precedentemente memorizzato.
     *
     * @param rawPassword    La password in chiaro inserita dall'utente in fase di autenticazione.
     * @param storedPassword La stringa del record memorizzata nel formato {@code saleBase64:hashBase64}.
     * @return {@code true} se la password coincide, {@code false} se è errata o se il formato memorizzato non è valido.
     */
    public static boolean verify(String rawPassword, String storedPassword) {
        try {
            if (storedPassword == null || storedPassword.isBlank()) {
                return false;
            }

            // Scompone il record nei due componenti originari
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            // Ricomputa l'hash sulla password inserita usando il medesimo sale
            byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt);

            /*
             * CRITICO: Utilizza MessageDigest.isEqual anziché Arrays.equals per effettuare
             * un confronto in tempo costante (constant-time), prevenendo attacchi basati sul tempo (timing attacks).
             */
            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            // Qualsiasi errore di decoding o formattazione invalida l'autenticazione in sicurezza
            return false;
        }
    }

    /**
     * Pipeline interna di derivazione crittografica della chiave.
     */
    private static byte[] pbkdf2(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

        KeySpec spec = new PBEKeySpec(
                password,
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        return factory.generateSecret(spec).getEncoded();
    }
}