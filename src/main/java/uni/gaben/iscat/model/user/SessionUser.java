package uni.gaben.iscat.model.user;

import java.time.LocalDateTime;

/**
 * Record immutabile che rappresenta la proiezione sicura dell'entità Utente per la sessione attiva.
 * Mantiene i dettagli identificativi e temporali dell'utente correntemente autenticato nell'applicazione,
 * omettendo intenzionalmente le informazioni sensibili come l'hash della password per garantire
 * la sicurezza dei dati all'interno dei layer di presentazione e logica di business.
 *
 * @param id             L'identificativo univoco numerico dell'utente associato alla sessione corrente.
 * @param username       Il nome utente del profilo autenticato, utilizzato per la personalizzazione dell'interfaccia.
 * @param dateOfCreation La marcatura temporale (data e ora) di registrazione iniziale dell'account.
 * @param lastLogin      La marcatura temporale (data e ora) dell'ultimo accesso registrato a sistema per l'utente.
 */
public record SessionUser(
        int id,
        String username,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {}