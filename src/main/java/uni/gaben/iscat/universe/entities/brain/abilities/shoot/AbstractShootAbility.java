package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.brain.abilities.Ability;
import uni.gaben.iscat.universe.entities.brain.abilities.AbilityCategory;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.utils.Cooldown;
import java.util.Set;

/**
 * Classe astratta di base per l'implementazione delle abilità di sparo e attacco balistico delle IA (Shoot Ability).
 * Coordina i cicli di cooldown, la verifica dei vincoli di ingaggio spaziale (combat range) e gli algoritmi di
 * predizione cinematica del bersaglio per determinare gli angoli di puntamento dei proiettili.
 */
public abstract class AbstractShootAbility extends Ability {

    protected final Cooldown cooldown;
    protected final double combatRange;
    protected final ProjectileType bulletType;
    protected final Target target;
    protected final boolean aimAtTarget;
    protected final int attackStateIndex;

    /** Vettore di posizione del bersaglio calcolato a runtime (ottimizzato per riutilizzo della memoria). */
    protected Vector2 targetPos = UU.vector2zero();

    /** Fattore moltiplicativo di errore introdotto intenzionalmente per depotenziare (nerf) la precisione della mira IA. */
    private final double nerfPrediction;

    /**
     * Costruttore protetto per l'inizializzazione dei parametri balistici ed operativi dell'abilità.
     */
    protected AbstractShootAbility(String name, double combatRange, double cooldownSec,
                                   ProjectileType bulletType, Target target, boolean aimAtTarget,
                                   double nerfPrediction, int attackStateIndex) {
        super(name, AbilityCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.bulletType = bulletType;
        this.target = target;
        this.aimAtTarget = aimAtTarget;
        this.nerfPrediction = nerfPrediction;
        this.attackStateIndex = attackStateIndex;
    }

    /**
     * Attiva l'abilità aggiornando la macchina a stati (FSM) dell'entità verso lo stato di attacco designato.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        if (brain.getEntity() instanceof EntityModel model) {
            EntityState directState = (this.attackStateIndex == 5)
                    ? EntityState.SPAWN_ATTACK
                    : EntityState.ATTACK;

            model.setEntityState(directState, this.attackStateIndex);
        }
    }

    /**
     * Verifica se l'abilità è pronta per essere attivata validando il timer di cooldown e il raggio di combattimento.
     * * @param self  L'entità fisica che sta tentando di attivare l'azione.
     * @param world Il modello dell'universo per localizzare i vettori spaziali del target.
     * @param dt    Il delta time del frame corrente per l'avanzamento dei timer.
     * @return {@code true} se i vincoli di distanza e tempo sono soddisfatti, altrimenti {@code false}.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        targetPos.set(0, 0);
        cooldown.update(dt);
        if (cooldown.isCoolingDown()) return false;

        targetPos = target.getPosition(world);
        if (targetPos == null) return false;

        if (combatRange >= 0) {
            double dist = self.getTransform().getTranslation().distance(targetPos);
            return !(dist > combatRange);
        }
        return true;
    }

    /**
     * Calcola l'angolo assoluto di puntamento ottimale per intercettare il bersaglio in movimento.
     * Integra un sistema di intercettazione predittiva calibrato in base alla velocità del proiettile ed al coefficiente di nerf.
     *
     * @param velocity La velocità nominale del proiettile emesso.
     * @return L'orientamento angolare in radianti verso cui indirizzare lo sparo.
     */
    protected double getAimAngle(Brain<?> brain, UniverseModel universe, double velocity) {
        targetPos.set(0, 0);
        if (aimAtTarget) {
            // Calcola la posizione futura stimata del bersaglio applicando la correzione sul vettore velocità
            target.predictedPosition(universe, brain.getEntity().getTransform().getTranslation(), velocity * (1 + nerfPrediction), targetPos);
            return targetPos != null ? brain.angleToTarget(targetPos) : brain.getEntity().getTransform().getRotationAngle();
        } else {
            return brain.getEntity().getTransform().getRotationAngle();
        }
    }

    /**
     * Aggiorna dinamicamente la durata predefinita del timer di cooldown dell'arma.
     */
    public void setCooldown(double v) {
        cooldown.setDefaultDuration(v);
    }
}