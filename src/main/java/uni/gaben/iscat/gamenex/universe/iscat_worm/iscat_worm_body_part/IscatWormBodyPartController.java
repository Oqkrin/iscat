package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormBodyPartController extends AiBehaviours<IscatWormBodyPartModel> {

    private final IscatWormBodyPartModel bodyPart;
    private IscatWormSegment previousSegment;

    public IscatWormBodyPartController(IscatWormBodyPartModel bodyPart) {
        super(bodyPart);
        this.bodyPart = bodyPart;
    }

    public void setPreviousSegment(IscatWormSegment previous) {
        this.previousSegment = previous;
    }

    /**
     * Imposta il segmento che questo body part deve seguire
     */
    public void setPreviousSegment(IscatWormBodyPartModel previous) {
        this.previousSegment = previous;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (bodyPart == null || bodyPart.isConsumed() || previousSegment == null) return;

        Vector2 myPos = bodyPart.getTransform().getTranslation();
        Vector2 prevPos = previousSegment.getPosition();

        Vector2 direction = prevPos.copy().subtract(myPos);
        double distance = direction.getMagnitude();

        double desiredDistance = IscatWormBodyPartSettings.FOLLOW_DISTANCE / UniverseSettings.SCALE;

        // FORZA MOLTO AGGRESSIVA se si allontana
        if (distance > desiredDistance) {
            double excess = distance - desiredDistance;

            Vector2 force = direction.getNormalized()
                    .multiply(IscatWormBodyPartSettings.FOLLOW_FORCE * (1 + excess * 8)); // correzione extra

            bodyPart.applyForce(force);

            // Rotazione molto reattiva
            double targetAngle = direction.getDirection();
            double currentAngle = bodyPart.getTransform().getRotationAngle();
            double newAngle = Interpolator.smootherStep(currentAngle, targetAngle, 0.28);
            bodyPart.getTransform().setRotation(newAngle);
        }

        // Limita velocità
        Vector2 vel = bodyPart.getLinearVelocity();
        if (vel.getMagnitude() > IscatWormBodyPartSettings.MAX_VELOCITY_MS) {
            bodyPart.setLinearVelocity(vel.getNormalized()
                    .multiply(IscatWormBodyPartSettings.MAX_VELOCITY_MS));
        }
    }
}