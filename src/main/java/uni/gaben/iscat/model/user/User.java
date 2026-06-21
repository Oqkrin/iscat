package uni.gaben.iscat.model.user;

import java.time.LocalDateTime;

/**
 * Record immutabile che rappresenta l'entità Utente all'interno del sistema ISCAT.
 * Mantiene i dati anagrafici essenziali, le credenziali cifrate e i riferimenti
 * temporali relativi al ciclo di vita del profilo utente nel database.
 *
 * @param id             L'identificativo univoco numerico dell'utente registrato nel database relazionale.
 * @param username       Il nome utente scelto in fase di registrazione, utilizzato come credenziale di accesso.
 * @param passwordHash   L'impronta crittografica (hash) della password dell'utente per la verifica sicura dell'identità.
 * @param dateOfCreation La marcatura temporale (data e ora) che indica l'esatto momento di creazione dell'account.
 * @param lastLogin      La marcatura temporale (data e ora) dell'ultima autenticazione eseguita con successo nel sistema.
 */
public record User(
        int id,
        String username,
        String passwordHash,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {
}