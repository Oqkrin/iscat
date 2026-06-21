package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Predictor;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.utils.Cooldown;
import java.util.Collections;
import java.util.List;

/**
 * Abilità di IA per l'affondo cinematico e lo scatto predittivo verso un bersaglio (Plunge Ability).
 * <p>
 * Interroga il sistema di tracciamento per calcolare il tempo ottimale di inseguimento (Pursuit Time)
 * attraverso i metodi matriciali della classe {@link Predictor}. Estrapola la posizione futura stimata
 * del target entro una finestra temporale massima, calcola il vettore direzionale d'intercettazione e applica
 * un forte impulso fisico lineare azzerando temporaneamente l'attrito (damping) del corpo rigido.
 * </p>
 */
public class PlungeAbility extends Ability {

    private final Cooldown plungeCooldown;
    private final Cooldown plungeDuration;
    private final Target plungeTarget;
    private final double maxPredictionTime;
    private final double plungeImpulse;

    private final Vector2 plungeDirection = new Vector2();

    /**
     * Inizializza l'abilità di affondo direzionale configurando le metriche di calcolo predittivo.
     *
     * @param cooldownSec       Secondi di attesa richiesti prima che l'affondo possa essere riutilizzato.
     * @param durationSec       Durata temporale espressa in secondi dell'effetto di accelerazione dello scatto.
     * @param maxPredictionTime Orizzonte temporale massimo in secondi per la proiezione della traiettoria del target.
     * @param impulse           Magnitudo dell'impulso di spinta applicato istantaneamente al corpo rigido.
     * @param target            Il fornitore del bersaglio verso cui indirizzare l'attacco (solitamente il giocatore).
     */
    public PlungeAbility(AbstractPhysicalEntityModel entity,
                         double cooldownSec,
                         double durationSec,
                         double maxPredictionTime,
                         double impulse,
                         Target target) {
        super("plunge", AbilityCategory.MOVEMENT, Collections.emptySet());
        this.plungeCooldown = new Cooldown(durationSec + cooldownSec);
        this.plungeDuration = new Cooldown(durationSec);
        this.maxPredictionTime = maxPredictionTime;
        this.plungeImpulse = impulse;
        this.plungeTarget = target;
        plungeCooldown.start();
    }

    /**
     * Verifica la disponibilità dell'azione convalidando lo stato dei timer di ricarica e di esecuzione corrente.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        if (plungeCooldown.isCoolingDown()) return false;
        return !plungeDuration.isCoolingDown();
    }

    /**
     * Attiva l'azione di affondo. Incrementa la velocità limite dell'entità, calcola il tempo di intercettazione
     * tramite matrici cinematiche, estrapola le coordinate future del bersaglio ed immette l'impulso fisico nel motore.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractPhysicalEntityModel self = brain.getEntity();
        self.setTemporaryTerminalVelocity(self.getTerminalVelocity() * 3);
        self.setDashLinearDamping(0);

        List<? extends AbstractPhysicalEntityModel> targets = plungeTarget.getEntities(world);
        if (targets == null || targets.isEmpty()) return;

        AbstractPhysicalEntityModel targetEntity = targets.getFirst();
        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 targetPos = targetEntity.getTransform().getTranslation();

        // Calcolo Matrimoniale/Cinematico del Tempo di Intercettazione (Pursuit Time)
        double currentSpeed = self.getLinearVelocity().getMagnitude();
        double maxVel = self.getTerminalVelocity();
        double pursuitTime = Predictor.calculatePursuitTime(
                selfPos,
                self.getTransform().getRotationAngle(),
                targetPos,
                targetEntity.getTransform().getRotationAngle(),
                currentSpeed,
                maxVel
        );

        // Troncamento del tempo calcolato entro la soglia di stabilità dell'orizzonte predittivo
        double lookAhead = Math.min(pursuitTime, maxPredictionTime);

        // Estrapolazione lineare della posizione del target al tempo normalizzato (t = lookAhead)
        Vector2 predictedPos = UU.vector2zero();
        Predictor.extrapolate(plungeTarget, world, lookAhead, predictedPos);

        // Elaborazione del vettore d'attacco ed applicazione dell'impulso sul baricentro
        plungeDirection.set(predictedPos).subtract(selfPos).normalize();
        self.applyImpulse(plungeDirection.multiply(plungeImpulse));

        plungeDuration.start();
        plungeCooldown.start();
    }

    /**
     * Monitora la progressione dello scatto. All'esaurimento della finestra di attività dell'abilità,
     * ripristina i parametri standard di attrito dinamico (linear damping) e velocità massima (terminal velocity).
     * * @return {@code true} se la fase di affondo è tuttora in corso, altrimenti {@code false}.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        if (plungeDuration.isReady()) {
            brain.getEntity().restoreLinearDamping();
            brain.getEntity().restoreTerminalVelocity();
        }
        return plungeDuration.isCoolingDown();
    }

    /**
     * Routine di update dedicata al decremento e all'avanzamento dei clock temporali di cooldown e durata dello scatto.
     */
    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {
        if (plungeCooldown.isCoolingDown()) {
            plungeCooldown.update(dt);
        }
        if (plungeDuration.isCoolingDown()) {
            plungeDuration.update(dt);
        }
    }
}