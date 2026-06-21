package uni.gaben.iscat.universe.entities.interfaces;

import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import java.util.function.Consumer;

/**
 * Interfaccia per la gestione reattiva dei contatti e delle collisioni fisiche (Collidable Pipeline).
 * <p>
 * Fornisce un'architettura basata su callback registrati (Pattern Observer) che permette a moduli esterni
 * (es. abilità come Melee o Kamikaze) di agganciare comportamenti personalizzati al verificarsi di un impatto,
 * senza dover estendere o modificare la classe fisica di base dell'entità.
 * </p>
 */
public interface Collidable {

    /**
     * Registra un listener di collisione associandolo a un identificativo univoco.
     *
     * @param id          La chiave stringa univoca per tracciare e sovrascrivere/rimuovere il callback.
     * @param onCollision Il consumer funzionale che definisce l'azione da eseguire, ricevendo l'entità colpita come parametro.
     */
    void addOnCollision(String id, Consumer<AbstractPhysicalEntityModel> onCollision);

    /**
     * Innesca ed esegue in sequenza tutti i callback di collisione attualmente registrati su questa entità.
     * Viene invocato internamente dal risolutore dei contatti dell'universo quando viene rilevato un impatto fisico.
     *
     * @param other L'istanza dell'altra entità fisica coinvolta nello scontro.
     */
    void triggerAllCollisions(AbstractPhysicalEntityModel other);

    /**
     * Svuota integralmente il registro dei listener di collisione, interrompendo qualsiasi notifica di impatto futura.
     */
    void clearOnCollisions();
}