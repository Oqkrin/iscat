package uni.gaben.iscat.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class PasswordHasher {

    private static final SecureRandom RNG =
            new SecureRandom();

    private static final int SALT_LENGTH = 16;

    // OWASP-ish modern value
    private static final int ITERATIONS = 600_000;

    // bits
    private static final int KEY_LENGTH = 256;

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private PasswordHasher() {}

    /**
     * Generates:
     * salt:hash
     */
    public static String hash(String rawPassword) {

        try {

            byte[] salt = new byte[SALT_LENGTH];
            RNG.nextBytes(salt);

            byte[] hash =
                    pbkdf2(rawPassword.toCharArray(), salt);

            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies a raw password against:
     * salt:hash
     */
    public static boolean verify(
            String rawPassword,
            String storedPassword
    ) {

        try {

            if (storedPassword == null || storedPassword.isBlank()) {
                return false;
            }

            String[] parts = storedPassword.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt =
                    Base64.getDecoder().decode(parts[0]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[1]);

            byte[] actualHash =
                    pbkdf2(
                            rawPassword.toCharArray(),
                            salt
                    );

            return MessageDigest.isEqual(
                    expectedHash,
                    actualHash
            );

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(
            char[] password,
            byte[] salt
    ) throws Exception {

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(ALGORITHM);

        KeySpec spec =
                new PBEKeySpec(
                        password,
                        salt,
                        ITERATIONS,
                        KEY_LENGTH
                );

        return factory.generateSecret(spec)
                .getEncoded();
    }
}