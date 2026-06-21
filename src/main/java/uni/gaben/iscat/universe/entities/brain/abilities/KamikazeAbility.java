package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Abilità di attacco ad impatto distruttivo per entità suicide (Kamikaze Ability).
 * <p>
 * Non viene attivata dalla coda delle azioni della FSM (canActivate restituisce sempre {@code false}),
 * ma registra un listener di collisione asincrono sul corpo rigido del motore fisico. All'atto dell'impatto,
 * se il corpo colliso soddisfa il filtro dei bersagli (Predicate), infligge il danno configurato tramite
 * l'interfaccia {@link Alterable} e marca l'entità stessa per la rimozione immediata dal mondo di gioco.
 * </p>
 */
public class KamikazeAbility extends Ability {

    /**
     * Inizializza l'abilità registrando il callback di collisione distruttiva standard.
     *
     * @param self    L'entità fisica che possiede l'abilità e che si autodistruggerà all'impatto.
     * @param damage  La quantità di danno diretto da infliggere al bersaglio.
     * @param targets Il predicato logico per filtrare e convalidare i corpi rigidi considerati bersagli validi.
     */
    public KamikazeAbility(AbstractPhysicalEntityModel self, double damage, Predicate<Body> targets) {
        this(self, damage, targets, "Kamikaze", AbilityCategory.SPECIAL, Collections.emptySet());
    }

    /**
     * Costruttore protetto per estendere la logica di impatto su classi derivate o personalizzate.
     */
    protected KamikazeAbility(AbstractPhysicalEntityModel self, double damage, Predicate<Body> targets, String name, AbilityCategory category, Set<AbilityCategory> blockedCategories) {
        super(name, category, blockedCategories);

        // Registrazione del Callback di Collisione sul Motore Fisico
        self.addOnCollision(name, entityModel -> {
            // Verifica se l'entità colpita rientra nello spettro dei target validi
            if (targets.test(entityModel)) {
                if (entityModel instanceof Alterable al) {
                    al.damage(damage);
                }
                // Forza la rimozione dell'entità dal ciclo logico e fisico dell'universo
                self.setShouldRemove(true);
            }
        });
    }

    /**
     * @return {@code false} in quanto l'abilità è passiva ed è interamente delegata ai callback di contatto del motore fisico.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        return false;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {}

    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}
}