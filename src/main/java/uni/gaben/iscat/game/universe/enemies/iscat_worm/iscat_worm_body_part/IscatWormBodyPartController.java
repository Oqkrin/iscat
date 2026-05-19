package uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_body_part;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormBodyPartController extends AiBehaviours<IscatWormBodyPartModel> {

    private final IscatWormBodyPartModel bodyPart;
    private IscatWormSegment previousSegment;

    public IscatWormBodyPartController(IscatWormBodyPartModel bodyPart) {
        super(bodyPart);
        this.bodyPart = bodyPart;

        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                if (IscatWormBodyPartController.this.bodyPart == null || IscatWormBodyPartController.this.bodyPart.isConsumed() || previousSegment == null) return;

                Vector2 myPos = IscatWormBodyPartController.this.bodyPart.getTransform().getTranslation();
                Vector2 prevPos = previousSegment.getPosition();

                Vector2 direction = prevPos.copy().subtract(myPos);
                double distance = direction.getMagnitude();

                double desiredDistance = UU.pxToM(IscatWormBodyPartSettings.FOLLOW_DISTANCE);

                // FORZA MOLTO AGGRESSIVA se si allontana
                if (distance > desiredDistance) {
                    double excess = distance - desiredDistance;

                    Vector2 force = direction.getNormalized()
                            .multiply(IscatWormBodyPartSettings.FOLLOW_FORCE * (1 + excess * 8)); // correzione extra

                    IscatWormBodyPartController.this.bodyPart.applyForce(force);

                    // Rotazione molto reattiva
                    double targetAngle = direction.getDirection();
                    double currentAngle = IscatWormBodyPartController.this.bodyPart.getTransform().getRotationAngle();
                    double newAngle = Interpolator.smootherStep(currentAngle, targetAngle, 0.28);
                    IscatWormBodyPartController.this.bodyPart.getTransform().setRotation(newAngle);
                }

                // Limita velocità
                Vector2 vel = IscatWormBodyPartController.this.bodyPart.getLinearVelocity();
                if (vel.getMagnitude() > IscatWormBodyPartSettings.MAX_VELOCITY_MS) {
                    IscatWormBodyPartController.this.bodyPart.setLinearVelocity(vel.getNormalized()
                            .multiply(IscatWormBodyPartSettings.MAX_VELOCITY_MS));
                }
            }
        });
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
        if (bodyPart == null || bodyPart.isConsumed() || previousSegment == null) return;
        super.aiUpdate(universeModel, dt);
    }
}