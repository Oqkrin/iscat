package uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_body_part;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_head.IscatWormHeadModel;
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

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (bodyPart == null || bodyPart.isConsumed()) return;

        // === PROMOZIONE A HEAD SE IL SEGMENTO PRECEDENTE È MORTO ===
        if (previousSegment == null ||
                (previousSegment instanceof LivingEntityModel living && living.shouldRemove())) {

            promoteToNewHead(universeModel);
            return;
        }

        // === FOLLOW NORMALE ===
        Vector2 myPos = bodyPart.getTransform().getTranslation();
        Vector2 prevPos = previousSegment.getPosition();

        Vector2 direction = prevPos.copy().subtract(myPos);
        double distance = direction.getMagnitude();

        double desiredDistance = UU.pxToM(IscatWormBodyPartSettings.FOLLOW_DISTANCE);

        if (distance > desiredDistance) {
            double excess = distance - desiredDistance;
            Vector2 force = direction.getNormalized()
                    .multiply(IscatWormBodyPartSettings.FOLLOW_FORCE * (1 + excess * 8));

            bodyPart.applyForce(force);

            // Rotazione
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

    private void promoteToNewHead(UniverseModel universeModel) {
        System.out.println("[IscatWorm] BodyPart promossa a nuova Head!");

        IscatWormHeadModel newHead = new IscatWormHeadModel(
                bodyPart.getTransform().getTranslationX(),
                bodyPart.getTransform().getTranslationY()
        );

        newHead.setLife(bodyPart.getLife());
        newHead.setLinearVelocity(bodyPart.getLinearVelocity());
        newHead.getTransform().setRotation(bodyPart.getTransform().getRotationAngle());

        // Rimuovi vecchia body part
        universeModel.removeEntity(bodyPart);

        // Aggiungi nuova Head
        universeModel.addEntity(newHead);

        // TODO: Aggiungi il controller della nuova Head
        // universeController.addAiController(new IscatWormHeadController(newHead));
    }
}