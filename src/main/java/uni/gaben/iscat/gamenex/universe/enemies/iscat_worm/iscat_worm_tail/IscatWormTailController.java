package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_tail;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormTailController extends AiBehaviours<IscatWormTailModel> {

    private final IscatWormTailModel tail;

    // Può seguire sia una BodyPart che la Head
    private IscatWormSegment previousSegment;  // può essere IscatWormHeadModel o IscatWormBodyPartModel

    public IscatWormTailController(IscatWormTailModel tail) {
        super(tail);
        this.tail = tail;

        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                if (IscatWormTailController.this.tail == null || IscatWormTailController.this.tail.isConsumed() || previousSegment == null) return;

                Vector2 myPos = IscatWormTailController.this.tail.getTransform().getTranslation();
                Vector2 prevPos = previousSegment.getPosition();

                Vector2 direction = prevPos.copy().subtract(myPos);
                double distance = direction.getMagnitude();

                double desiredDistance = IscatWormTailSettings.FOLLOW_DISTANCE / UniverseSettings.SCALE;

                // FORZA MOLTO AGGRESSIVA se si allontana
                if (distance > desiredDistance) {
                    double excess = distance - desiredDistance;

                    Vector2 force = direction.getNormalized()
                            .multiply(IscatWormTailSettings.FOLLOW_FORCE * (1 + excess * 8)); // correzione extra

                    IscatWormTailController.this.tail.applyForce(force);

                    // Rotazione molto reattiva
                    double targetAngle = direction.getDirection();
                    double currentAngle = IscatWormTailController.this.tail.getTransform().getRotationAngle();
                    double newAngle = Interpolator.smootherStep(currentAngle, targetAngle, 0.28);
                    IscatWormTailController.this.tail.getTransform().setRotation(newAngle);
                }

                // Limita velocità
                Vector2 vel = IscatWormTailController.this.tail.getLinearVelocity();
                if (vel.getMagnitude() > IscatWormTailSettings.MAX_VELOCITY_MS) {
                    IscatWormTailController.this.tail.setLinearVelocity(vel.getNormalized()
                            .multiply(IscatWormTailSettings.MAX_VELOCITY_MS));
                }
            }
        });
    }

    /**
     * Imposta il segmento precedente (può essere Head o BodyPart)
     */
    public void setPreviousSegment(IscatWormSegment previous) {
        this.previousSegment = previous;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (tail == null || tail.isConsumed() || previousSegment == null) return;
        super.aiUpdate(universeModel, dt);
    }
}