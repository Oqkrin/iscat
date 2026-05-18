package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormHeadController extends AiBehaviours<IscatWormHeadModel> {

    private final IscatWormHeadModel head;
    private Vector2 target = null;

    public IscatWormHeadController(IscatWormHeadModel head) {
        super(head);
        this.head = head;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 headPos = head.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        // Aggiorna target sul player
        if (target == null || headPos.distanceSquared(target) < 12) {
            target = playerPos.copy();
        }

        Vector2 direction = target.copy().subtract(headPos);
        double distance = direction.getMagnitude();

        // === ATTACCO ===
        double attackRadius = IscatWormHeadSettings.RAGGIO_COLLISIONE_PX
                / UniverseSettings.SCALE
                * IscatWormHeadSettings.ATTACK_RADIUS_MULTIPLIER;

        if (distance < attackRadius) {
            performAttack(player, universeModel);
            return;
        }

        // === INSEGUIMENTO AGGRESSIVO ===
        if (distance > 0.5) {
            // Rotazione fluida
            double targetAngle = direction.getDirection();
            double currentAngle = head.getTransform().getRotationAngle();
            double newAngle = Interpolator.smootherStep(currentAngle, targetAngle, 0.17);
            head.getTransform().setRotation(newAngle);

            // Forza
            if (head.getLinearVelocity().getMagnitude() <= IscatWormHeadSettings.MAX_VELOCITY_MS) {
                Vector2 force = direction.getNormalized()
                        .multiply(IscatWormHeadSettings.FORCE);

                head.applyForce(force);
            } else {
                // Limita velocità
                Vector2 vel = head.getLinearVelocity();
                head.setLinearVelocity(vel.getNormalized()
                        .multiply(IscatWormHeadSettings.MAX_VELOCITY_MS));
            }
        }
    }

    private void performAttack(PlayerModel player, UniverseModel universe) {
        System.out.println("[SnakeHead] ATTACCO al player!");

        player.setLife(player.getLife()- IscatWormHeadSettings.ATTACK_POWER);
    }
}