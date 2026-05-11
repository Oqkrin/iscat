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
     * Di Default insegue il player
     *
     * @param idealDistPx  Distanza bersaglio (pixel).
     * @param maxForce     Forza massima (Newton).
     * @param rampUpPx     Spazio di accelerazione (pixel).
     * @param maxVelocity  Velocità di crociera base (m/s).
     * @param steeringGain Reattività della sterzata.
     * @param minScale     Moltiplicatore velocità minima (vicino).
     * @param maxScale     Moltiplicatore velocità massima (lontano).
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
        // Passiamo il target come parametro locale (o recuperiamolo dal controller)
        Vector2 playerPos = universe.getPlayer().getTransform().getTranslation();
        executeOnTarget(npc, playerPos, dt);
    }

    public void executeOnCurrentDirection(AbstractEntityModel npc, UniverseModel universe, double dt) {
        chase(npc, 1, Vector2.create(npc.getLinearVelocity().getMagnitude(), npc.getTransform().getRotationAngle()), 1);
    }

    public void executeOnTarget(AbstractEntityModel npc, Vector2 targetPos, double dt) {
        Vector2 myPos = npc.getTransform().getTranslation();
        double dist = myPos.distance(targetPos);

        // Calcoliamo la direzione basata sulla rotazione attuale (come nel tuo codice)
        double currentAngle = npc.getTransform().getRotationAngle();
        Vector2 forwardDir = new Vector2(currentAngle);

        // 1. Decidiamo se andare avanti o indietro
        double sign = (dist < idealDistMeters) ? -1.0 : 1.0;

        chase(npc, dist, forwardDir, sign);
    }

    private void chase(AbstractEntityModel npc, double dist, Vector2 forwardDir, double sign) {
        // 2. Applichiamo la forza
        // Invece di usare smootherStep con dt, usiamo una forza costante se siamo fuori range.
        // Se vuoi smoothing, dovresti interpolare la MAGNITUDO della forza, non i segni.
        if (Math.abs(dist - idealDistMeters) > 0.1) {
            // Applichiamo la spinta
            Vector2 push = forwardDir.product(sign * maxForce);
            npc.applyForce(push);

            // 3. Damping dinamico
            // Invece di 40.0 (che è un muro), usiamo un valore che freni il mob
            // solo quando è vicino al player o quando deve cambiare direzione.
            npc.setLinearDamping(2.0);
        } else {
            // Se siamo "arrivati", freniamo più bruscamente per stabilizzarci
            npc.setLinearDamping(10.0);
        }
    }
}
