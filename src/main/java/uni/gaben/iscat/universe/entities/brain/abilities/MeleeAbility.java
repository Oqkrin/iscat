package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.utils.Cooldown;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Abilità per la gestione degli attacchi fisici a corto raggio o da impatto (Melee Ability).
 * <p>
 * Sfrutta il sistema di collisione del motore fisico per rilevare i contatti con i corpi idonei (Predicate).
 * Il danno inflitto non è fisso, ma viene scalato dinamicamente in base all'energia cinetica dell'entità al momento
 * dell'impatto, moltiplicando il danno base per la magnitudo della velocità lineare. L'esecuzione dell'attacco
 * e il reset del timer sono regolati in modo sincrono attraverso la FSM delle azioni.
 * </p>
 *
 * @param <T> Il tipo specifico di entità fisica che possiede ed esegue l'attacco melee.
 */
public class MeleeAbility<T extends AbstractPhysicalEntityModel> extends Ability {

    private final Cooldown meleeCooldown = new Cooldown();

    /**
     * Inizializza l'azione melee registrando il listener di collisione e agganciando il calcolo cinematico del danno.
     *
     * @param name     Il nome identificativo dell'azione.
     * @param entity   L'istanza dell'entità attaccante.
     * @param cooldown Il tempo di attesa in secondi richiesto tra un colpo e il successivo.
     * @param damage   Il modificatore di danno base dell'attacco.
     * @param targets  Il predicato logico per filtrare e convalidare le entità colpite.
     */
    public MeleeAbility(String name, T entity, double cooldown, double damage, Predicate<Body> targets) {
        super(name, AbilityCategory.SPECIAL, Collections.emptySet());
        this.meleeCooldown.setDefaultDuration(cooldown);

        // --- Registrazione del Callback d'Impatto sul Motore Fisico ---
        entity.addOnCollision(
                "Melee",
                other -> {
                    // Verifica la prontezza del cooldown e la validità del bersaglio vivente
                    if (meleeCooldown.isReady() && targets.test(other) && other instanceof AbstractLivingEntityModel l) {
                        // Formula cinetica: Danno = DannoBase * (VelocitàCorrente / 5)
                        l.damage(damage * entity.getLinearVelocity().getMagnitude() / 5);
                    }
                }
        );
    }

    /**
     * Verifica se la finestra temporale di ricarica dell'attacco è terminata.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        return meleeCooldown.isReady();
    }

    /**
     * Avvia ufficialmente il timer di cooldown nel frame in cui l'azione melee viene estratta dalla coda.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        meleeCooldown.start();
    }

    /**
     * Aggiorna lo stato del cooldown frame dopo frame. Mantiene l'azione attiva nella coda logica
     * per l'intera durata del ciclo di aggiornamento.
     * * @return {@code true} in modo continuo per preservare il ciclo di update del cooldown sulla FSM.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        meleeCooldown.update(dt);
        return true;
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}
}