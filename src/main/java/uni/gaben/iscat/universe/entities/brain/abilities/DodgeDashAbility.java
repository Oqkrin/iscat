package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.utils.Cooldown;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Abilità di IA per l'evasione tattica e lo scatto direzionale difensivo (Dodge Dash Ability).
 * <p>
 * Analizza costantemente i vettori di posizione e velocità delle minacce entomologiche o proiettili nel mondo.
 * Utilizza un algoritmo predittivo per calcolare il punto di massimo avvicinamento (CPA); se viene rilevato un impatto
 * imminente entro un raggio di tolleranza, calcola una traiettoria di fuga ortogonale/opposta, applica un impulso fisico
 * di scatto e corregge il vettore per forzare l'entità a rimanere entro i confini visivi della telecamera (Viewport Bounding).
 * </p>
 */
public class DodgeDashAbility extends Ability {

    private final Cooldown dashCooldown;
    private final Cooldown dashDuration;
    private final double maxPredictionTime;
    private final double avoidRadius;
    private final double dashImpulse;
    private final Target threatSupplier;

    private final Random rand = new Random();
    private final Vector2 dashDirection = new Vector2();

    /**
     * Inizializza l'abilità di schivata predittiva configurando le soglie di intercettazione e l'intensità dello scatto.
     */
    public DodgeDashAbility(AbstractPhysicalEntityModel entity, double cooldownSec, double durationSec, double maxPredictionTime,
                            double avoidRadius, double dashImpulse, Target threatSupplier) {
        super("dodgeDash", AbilityCategory.MOVEMENT, Collections.emptySet());
        this.dashCooldown = new Cooldown(durationSec + cooldownSec);
        this.dashDuration = new Cooldown(durationSec);
        this.maxPredictionTime = maxPredictionTime;
        this.avoidRadius = avoidRadius;
        this.dashImpulse = dashImpulse;
        this.threatSupplier = threatSupplier;
    }

    /**
     * Esegue lo screening proattivo delle minacce calcolando il tempo al punto di minimo approccio ($t$).
     * Risolve l'equazione vettoriale della distanza minima futura tra l'entità e le minacce basandosi sulle velocità relative.
     * * @return {@code true} se viene rilevata almeno una collisione imminente entro la finestra temporale di predizione.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        if (dashCooldown.isCoolingDown()) return false;
        if (dashDuration.isCoolingDown()) return false;

        List<AbstractPhysicalEntityModel> threats = threatSupplier.getEntities(world);
        if (threats == null || threats.isEmpty()) return false;

        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 selfVel = self.getLinearVelocity();
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

        double shortestTime = Double.MAX_VALUE;
        AbstractPhysicalEntityModel mostImminent = null;

        for (AbstractPhysicalEntityModel threat : threats) {
            if (threat == self || threat.shouldRemove()) continue;

            dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
            dv.set(threat.getLinearVelocity()).subtract(selfVel);

            double dvSq = dv.getMagnitudeSquared();
            if (dvSq < 0.0001) continue;

            // t = - (dp • dv) / ||dv||²
            double t = -dp.dot(dv) / dvSq;

            if (t > 0 && t < maxPredictionTime) {
                double cx = dp.x + (dv.x * t);
                double cy = dp.y + (dv.y * t);
                // Verifica se la distanza minima proiettata è inferiore al raggio di guardia (avoidRadius)
                if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                    if (t < shortestTime) {
                        shortestTime = t;
                        mostImminent = threat;
                    }
                }
            }
        }

        return mostImminent != null;
    }

    /**
     * Attiva lo scatto evasivo. Isola la minaccia più imminente, ne calcola la posizione futura stimata
     * e proietta un vettore di fuga opposto, applicando una perturbazione angolare casuale anti-pattern.
     * Effettua infine il clipping del vettore risultante per evitare che l'entità scatti fuori dallo schermo.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractPhysicalEntityModel self = brain.getEntity();
        self.setTemporaryTerminalVelocity(self.getTerminalVelocity() * 3);
        self.setDashLinearDamping(0);

        List<AbstractPhysicalEntityModel> threats = threatSupplier.getEntities(world);
        if (threats == null || threats.isEmpty()) return;

        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 selfVel = self.getLinearVelocity();
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

        double shortestTime = Double.MAX_VALUE;
        AbstractPhysicalEntityModel mostImminent = null;

        for (AbstractPhysicalEntityModel threat : threats) {
            if (threat == self || threat.shouldRemove()) continue;

            dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
            dv.set(threat.getLinearVelocity()).subtract(selfVel);

            double dvSq = dv.getMagnitudeSquared();
            if (dvSq < 0.0001) continue;

            double t = -dp.dot(dv) / dvSq;
            if (t > 0 && t < maxPredictionTime) {
                double cx = dp.x + (dv.x * t);
                double cy = dp.y + (dv.y * t);
                if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                    if (t < shortestTime) {
                        shortestTime = t;
                        mostImminent = threat;
                    }
                }
            }
        }

        if (mostImminent == null) return;

        // --- Proiezione Posizioni Future Cinematiche ---
        Vector2 threatFuture = UU.vector2zero();
        threatFuture.set(mostImminent.getTransform().getTranslation());
        threatFuture.add(mostImminent.getLinearVelocity().x * shortestTime, mostImminent.getLinearVelocity().y * shortestTime);

        Vector2 myFuture = UU.vector2zero();
        myFuture.set(selfPos);
        myFuture.add(selfVel.x * shortestTime, selfVel.y * shortestTime);

        // Vettore direzionale opposto alla traiettoria di impatto della minaccia
        dashDirection.set(myFuture).subtract(threatFuture).normalize();

        // Iniezione di rumore stocastico angolare (±15 gradi) per rompere la prevedibilità dell'IA
        double randomAngle = (rand.nextDouble() - 0.5) * Math.toRadians(30);
        dashDirection.rotate(randomAngle);

        // --- Vincolo di Contenimento nella Viewport della Telecamera ---
        CameraModel camera = world.getCamera();
        if (camera != null) {
            double zoom = camera.getZoom();
            double halfVW = (camera.getScreenWidth() / 2.0) / zoom;
            double halfVH = (camera.getScreenHeight() / 2.0) / zoom;
            double margin = UU.pxToM(50);

            double minX = camera.getX() - halfVW + margin;
            double maxX = camera.getX() + halfVW - margin;
            double minY = camera.getY() - halfVH + margin;
            double maxY = camera.getY() + halfVH - margin;

            double futureX = selfPos.x + dashDirection.x * (dashImpulse / self.getTerminalVelocity());
            double futureY = selfPos.y + dashDirection.y * (dashImpulse / self.getTerminalVelocity());

            // Inversione dei componenti vettoriali in caso di violazione dei bordi dello schermo (Rimbalzo Virtuale)
            if (futureX < minX && dashDirection.x < 0) dashDirection.x = -dashDirection.x;
            if (futureX > maxX && dashDirection.x > 0) dashDirection.x = -dashDirection.x;
            if (futureY < minY && dashDirection.y < 0) dashDirection.y = -dashDirection.y;
            if (futureY > maxY && dashDirection.y > 0) dashDirection.y = -dashDirection.y;

            dashDirection.normalize();
        }

        // Applicazione dell'impulso di spinta fIsica scalato per la massa dell'oggetto
        double mass = self.getMass().getMass();
        self.applyImpulse(dashDirection.multiply(dashImpulse * mass));

        dashDuration.start();
        dashCooldown.start();
    }

    /**
     * Monitora l'avanzamento dello scatto nel tempo. All'esaurimento della durata dell'effetto,
     * ripristina i coefficienti nominali di attrito (damping) e velocità limite dell'entità.
     * * @return {@code true} se la finestra temporale di dash è ancora attiva, {@code false} al completamento.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        if (dashDuration.isReady()) {
            brain.getEntity().restoreTerminalVelocity();
            brain.getEntity().restoreLinearDamping();
        }
        return dashDuration.isCoolingDown();
    }

    /**
     * Routine di update per l'avanzamento dei timer interni dei cooldown di attivazione e di durata.
     */
    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {
        if (dashCooldown.isCoolingDown()) {
            dashCooldown.update(dt);
        }
        if (dashDuration.isCoolingDown()) {
            dashDuration.update(dt);
        }
    }
}