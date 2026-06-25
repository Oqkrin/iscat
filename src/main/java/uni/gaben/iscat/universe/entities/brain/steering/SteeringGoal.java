package uni.gaben.iscat.universe.entities.brain.steering;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Predictor;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import java.util.List;

/**
 * Interfaccia funzionale per la definizione e il calcolo dei vettori di guida IA (Steering Goals).
 * <p>
 * Implementa i comportamenti di guida di Craig Reynolds (Pursuit, Evade, Range Keeping). I metodi generano
 * un vettore di accelerazione (forza di sterzata) risolvendo l'equazione fondamentale:
 * </p>
 * $$\text{SteeringForce} = \text{DesiredVelocity} - \text{CurrentVelocity}$$
 */
@FunctionalInterface
public interface SteeringGoal {

    /**
     * Calcola la forza o la velocità desiderata per guidare cinematicamente l'entità nel frame corrente.
     *
     * @param self     L'entità fisica che deve applicare la forza di steering.
     * @param universe Il modello dell'universo per tracciare le coordinate e le telecamere.
     * @param dt       Il delta time del frame corrente.
     * @return Il vettore di forza/accelerazione di sterzata risultante.
     */
    Vector2 computeDesiredVelocity(AbstractPhysicalEntityModel self, UniverseModel universe, double dt);

    /**
     * @return Un comportamento di quiete che restituisce un vettore nullo (nessuna forza applicata).
     */
    static SteeringGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (apme, universe, dt) -> idleVelocity;
    }

    /**
     * Inseguimento predittivo (Pursuit). Intercetta un bersaglio mobile calcolando la sua posizione
     * futura stimata (look-ahead) basandosi sulle velocità correnti delle due entità.
     *
     * @param target            Il fornitore del bersaglio da inseguire.
     * @param maxPredictionTime Limite superiore in secondi per l'estrapolazione della traiettoria futuribile.
     */
    static SteeringGoal pursuit(Target target, double maxPredictionTime) {
        Vector2 pursuitVelocity = UU.vector2zero();
        Vector2 pursuitPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return pursuitVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return pursuitVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculatePursuitTime(
                    selfPos, self.getTransform().getRotationAngle(),
                    targetPos, entity.getTransform().getRotationAngle(),
                    self.getLinearVelocity().getMagnitude(), self.getTerminalVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, pursuitPos);

            pursuitVelocity.set(pursuitPos).subtract(selfPos);

            if (!pursuitVelocity.isZero()) {
                pursuitVelocity.setMagnitude(self.getTerminalVelocity());
                pursuitVelocity.subtract(self.getLinearVelocity());

                if (!pursuitVelocity.isZero()) {
                    pursuitVelocity.setMagnitude(self.getAcceleration());
                }
            }

            return pursuitVelocity;
        };
    }

    /**
     * Evasione predittiva (Evade). Si allontana in modo proattivo da un bersaglio mobile proiettando
     * la traiettoria di fuga in base al punto di intercettazione stimato.
     *
     * @param target            La minaccia da cui fuggire.
     * @param maxPredictionTime Limite superiore in secondi per l'estrapolazione della traiettoria.
     */
    static SteeringGoal evade(Target target, double maxPredictionTime) {
        Vector2 evadeVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return evadeVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return evadeVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculateEvadeTime(
                    selfPos, targetPos, self.getTerminalVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            evadeVelocity.set(selfPos).subtract(predictedPos);

            if (!evadeVelocity.isZero()) {
                evadeVelocity.setMagnitude(self.getTerminalVelocity());
                evadeVelocity.subtract(self.getLinearVelocity());

                if (!evadeVelocity.isZero()) {
                    evadeVelocity.setMagnitude(self.getAcceleration());
                }
            }

            return evadeVelocity;
        };
    }

    /**
     * Inseguimento proporzionale con mantenimento della distanza (Pursuit with Range).
     * Mantiene l'entità all'interno di un intervallo di ingaggio ideale (min/max). Integra un sistema di controllo
     * proporzionale (gain) e un vincolo di clipping per evitare che l'entità indietreggi fuori dalla viewport della telecamera.
     * Inoltre, effettua il "velocity matching" quando si trova alla distanza corretta per scortare fluidamente il bersaglio.
     */
    static SteeringGoal pursuitWithRange(Target target, double maxPredictionTime,
                                         double minDistance, double maxDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos   = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return desiredVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculatePursuitTime(
                    selfPos, self.getTransform().getRotationAngle(),
                    targetPos, entity.getTransform().getRotationAngle(),
                    self.getLinearVelocity().getMagnitude(), self.getTerminalVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            desiredVelocity.set(predictedPos).subtract(selfPos);
            double distance = desiredVelocity.getMagnitude();
            double idealDistance = (minDistance + maxDistance) * 0.5;

            // Vincolo restrittivo dello spazio di manovra basato sui confini reali della telecamera (Viewport Bounding)
            CameraModel camera = universe.getCamera();
            if (camera != null) {
                double zoom = camera.getZoom();
                double halfVW = UU.pxToM((camera.getScreenWidth() / 2.0) / zoom);
                double halfVH = UU.pxToM((camera.getScreenHeight() / 2.0) / zoom);
                double minScreenDim = Math.min(halfVW, halfVH) - UU.pxToM(50.0);
                if (idealDistance > minScreenDim && minScreenDim > 0) {
                    idealDistance = minScreenDim;
                }
            }

            double error = distance - idealDistance;

            double gain = 3.0;
            double desiredSpeed;
            if (error > 0) {
                desiredSpeed = Math.clamp(gain * error, 0, self.getTerminalVelocity());
            } else {
                double backSpeed = 0.4 * self.getTerminalVelocity(); // Smorzamento pesante in retromarcia
                desiredSpeed = Math.clamp(gain * error, -backSpeed, 0);
            }

            if (distance > 0.0001) {
                desiredVelocity.normalize();
            } else {
                desiredVelocity.set(entity.getLinearVelocity());
                if (desiredVelocity.isZero()) desiredVelocity.set(1, 0);
            }

            // Attivazione della dead-zone metrica per prevenire oscillazioni o vibrazioni microscopiche
            double deadZone = 0.5;
            if (Math.abs(error) < deadZone) {
                return desiredVelocity.set(0, 0);
            }
            desiredVelocity.multiply(desiredSpeed);

            // Sincronizzazione vettoriale (Velocity Matching) basata sul coefficiente d'errore residuo
            double followFactor = 1.0 - Math.abs(error) / (maxDistance - minDistance + 0.0001);
            followFactor = Math.clamp(followFactor, 0.0, 1.0);
            desiredVelocity.add(entity.getLinearVelocity().copy().multiply(followFactor));

            desiredVelocity.subtract(self.getLinearVelocity());

            if (!desiredVelocity.isZero()) {
                desiredVelocity.setMagnitude(self.getAcceleration());
            }

            return desiredVelocity;
        };
    }

    /**
     * Evasione limitata da una soglia di guardia (Evade with Range).
     * Applica la fuga predittiva standard solo se il bersaglio scende al di sotto della distanza di sicurezza configurata.
     * Se l'entità è in zona sicura, neutralizza le forze di sterzata allineandosi alla velocità del target.
     *
     * @param safetyDistance Raggio limite della bolla di sicurezza entro cui si innesca la fuga accelerata.
     */
    static SteeringGoal evadeWithRange(Target target, double maxPredictionTime, double safetyDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return desiredVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculateEvadeTime(
                    selfPos, targetPos, self.getTerminalVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            desiredVelocity.set(selfPos).subtract(predictedPos);
            double distance = desiredVelocity.getMagnitude();

            if (distance < safetyDistance) {
                if (distance > 0) {
                    desiredVelocity.setMagnitude(self.getTerminalVelocity());
                } else {
                    desiredVelocity.set(1, 0).setMagnitude(self.getTerminalVelocity());
                }
            } else {
                desiredVelocity.set(entity.getLinearVelocity()); // Zona sicura: neutralizza le forze esterne
            }

            desiredVelocity.subtract(self.getLinearVelocity());

            if (!desiredVelocity.isZero()) {
                desiredVelocity.setMagnitude(self.getAcceleration());
            }

            return desiredVelocity;
        };
    }
}