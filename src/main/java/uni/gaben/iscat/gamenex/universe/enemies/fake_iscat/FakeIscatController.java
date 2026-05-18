package uni.gaben.iscat.gamenex.universe.enemies.fake_iscat;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileType;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER;

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<FakeIscatModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat);

        shooter = new Shooter<>(iscat);

        bulletTemplate = new Projectile();
        bulletTemplate.setType(ProjectileType.ENEMY_BULLET);
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (aiEntity == null || aiEntity.shouldRemove()) return;

        fireCooldown.update(dt);

        PlayerModel player = universeModel.getPlayer();

        double distToPlayer = player == null
                ? Double.MAX_VALUE
                : aiEntity.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());

        if (player == null || distToPlayer > FakeIscatSettings.DETECTION_RANGE) {
            state = State.WANDER;
        } else if (distToPlayer <= FakeIscatSettings.COMBAT_RANGE) {
            state = State.COMBAT;
        } else {
            state = State.CHASE;
        }

        switch (state) {
            case WANDER -> updateWander(dt);
            case CHASE -> updateChase(player, dt);
            case COMBAT -> updateCombat(player, dt);
        }
    }

    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();

            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }

        //rotateTo(wanderTarget.getDirection(), dt);

        aiEntity.applyForce(
                wanderTarget.getNormalized().multiply(FakeIscatSettings.FORCE)
        );

        if (aiEntity.contains(wanderTarget)) wanderTarget = null;
    }

    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);

        //rotateTo(toPlayer.getDirection(), dt);

        aiEntity.applyForce(
                toPlayer.getNormalized().multiply(FakeIscatSettings.FORCE)
        );
    }

    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        if (dist < FakeIscatSettings.PREFERRED_RANGE) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-FakeIscatSettings.FORCE * 0.6));
        } else if (dist > FakeIscatSettings.PREFERRED_RANGE * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(FakeIscatSettings.FORCE * 0.4));
        }

        //rotateTo(toPlayer.getDirection(), dt);

        if (!fireCooldown.isCoolingDown()) {
            shooter.shoot(bulletTemplate);
            fireCooldown.start(FakeIscatSettings.FIRE_COOLDOWN_S);
        }
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(FakeIscatSettings.ROTATION_SPEED * dt, 1.0)
        );

        aiEntity.getTransform().setRotation(next);
    }
}