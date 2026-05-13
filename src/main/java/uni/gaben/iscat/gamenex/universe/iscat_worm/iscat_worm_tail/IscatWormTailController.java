package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadModel; // se hai già la head
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormTailController extends AiBehaviours<IscatWormTailModel> {

    private final IscatWormTailModel tail;

    // Può seguire sia una BodyPart che la Head
    private IscatWormSegment previousSegment;  // può essere IscatWormHeadModel o IscatWormBodyPartModel

    public IscatWormTailController(IscatWormTailModel tail) {
        super(tail);
        this.tail = tail;
    }

    /**
     * Imposta il segmento precedente (può essere Head o BodyPart)
     */
    public void setPreviousSegment(IscatWormSegment previous) {
        this.previousSegment = previous;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (tail == null || tail.isConsumed() || previousSegment == null) return;

        Vector2 myPos = tail.getTransform().getTranslation();
        Vector2 prevPos;

        if (previousSegment instanceof IscatWormHeadModel head) {
            prevPos = head.getTransform().getTranslation();
        } else if (previousSegment instanceof IscatWormBodyPartModel body) {
            prevPos = body.getTransform().getTranslation();
        } else {
            return;
        }

        Vector2 direction = prevPos.copy().subtract(myPos);
        double distance = direction.getMagnitude();

        // Mantieni distanza dal segmento precedente
        if (distance > IscatWormTailSettings.FOLLOW_DISTANCE / UniverseSettings.SCALE) {
            Vector2 force = direction.getNormalized()
                    .multiply(IscatWormTailSettings.FOLLOW_FORCE);

            tail.applyForce(force);

            // Rotazione più morbida (coda floscia)
            double targetAngle = direction.getDirection();
            double currentAngle = tail.getTransform().getRotationAngle();
            double newAngle = Interpolator.smootherStep(currentAngle, targetAngle,
                    IscatWormTailSettings.ROTATION_SPEED);

            tail.getTransform().setRotation(newAngle);
        }

        // Limita velocità
        Vector2 vel = tail.getLinearVelocity();
        if (vel.getMagnitude() > IscatWormTailSettings.MAX_VELOCITY_MS) {
            tail.setLinearVelocity(vel.getNormalized()
                    .multiply(IscatWormTailSettings.MAX_VELOCITY_MS));
        }
    }
}