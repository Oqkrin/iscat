package uni.gaben.iscat.gamenex.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Comportamento di inseguimento con sterzata (Steering).
 * Calcola una forza per mantenere l'entità a una distanza ideale dal bersaglio.
 * Evita scatti bruschi utilizzando transizioni fluide.
 */
public class ChaseBehavior implements AiBehavior {
    private final double idealDistMeters;
    private final double maxForce;
    private final double rampUpMeters;
    private final double maxVelocity;
    private final double steeringGain;
    private final double minScale;
    private final double maxScale;

    /**
     * Crea un comportamento di inseguimento dinamico.
     * @param idealDistPx Distanza bersaglio (pixel).
     * @param maxForce Forza massima (Newton).
     * @param rampUpPx Spazio di accelerazione (pixel).
     * @param maxVelocity Velocità di crociera base (m/s).
     * @param steeringGain Reattività della sterzata.
     * @param minScale Moltiplicatore velocità minima (vicino).
     * @param maxScale Moltiplicatore velocità massima (lontano).
     */
    public ChaseBehavior(double idealDistPx, double maxForce, double rampUpPx, double maxVelocity, double steeringGain, double minScale, double maxScale) {
        this.idealDistMeters = idealDistPx / UniverseSettings.SCALE;
        this.maxForce = maxForce;
        this.rampUpMeters = rampUpPx / UniverseSettings.SCALE;
        this.maxVelocity = maxVelocity;
        this.steeringGain = steeringGain;
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        Vector2 target = universe.getPlayer().getTransform().getTranslation();
        Vector2 myPos = npc.getTransform().getTranslation();

        double dist = target.distance(myPos);
        Vector2 direction = myPos.to(target);
        direction.normalize();

        double diff = dist - idealDistMeters;
        double t = Math.clamp(diff / rampUpMeters, -1.0, 1.0);

        // Fattore di guida (1.0 = avanti tutta, -1.0 = retromarcia)
        double driveFactor = Interpolator.smoothStep(0, 1, Math.abs(t)) * Math.signum(t);

        Vector2 currentVel = npc.getLinearVelocity();
        double currentSpeed = currentVel.getMagnitude();

        if (driveFactor > 0) {
            // Se stiamo spingendo in avanti
            if (currentSpeed > maxVelocity) {
                // Se abbiamo superato il limite, non applichiamo nessuna forza.
                // L'attrito naturale (Damping) farà scendere la velocità dolcemente.
                return;
            }

            // Calcoliamo una forza che sfuma man mano che arriviamo alla top speed.
            // Se siamo al 90% della velocità, spingiamo solo con il 10% della forza.
            double speedMargin = Math.clamp(1.0 - (currentSpeed / maxVelocity), 0.0, 1.0);
            double finalForceMagnitude = maxForce * driveFactor * speedMargin;

            npc.applyForce(direction.product(finalForceMagnitude));
        } else if (driveFactor < 0) {
            // Se dobbiamo frenare/andare in retro, applichiamo la forza normalmente
            npc.applyForce(direction.product(driveFactor * maxForce));
        }
    }
}
