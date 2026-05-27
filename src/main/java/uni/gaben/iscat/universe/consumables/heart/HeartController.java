package uni.gaben.iscat.universe.consumables.heart;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.player.PlayerModel;

public class HeartController extends AiController {

    private final HeartModel heart;
    private boolean collected = false;

    public HeartController(HeartModel heart) {
        super(heart, heart.getBaseAccelerationPerTick(), heart.getTerminalVelocity(), 0.0);
        this.heart = heart;

        // Collision callback
        this.heart.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !collected) {
                collected = true;
                double halfMaxLife = player.getMaxLife() / 2.0;
                player.deltaToLife(halfMaxLife);
                heart.setLife(0);
                heart.kill();
                heart.setShouldRemove(true);
            }
        });

        // Movement: follow the player when nearby
        setMovementStrategy(new HeartMovement());

        // NO modifiers – the heart ignores obstacles, projectiles, and separation
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (heart == null || heart.shouldRemove() || collected) return;
        super.update(universe, dt);
    }

    // ── Movement strategy ─────────────────────────────────────────────

    private class HeartMovement implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            if (collected) return new Vector2();

            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 heartPos = heart.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            double dist = heartPos.distance(playerPos);

            if (dist < HeartSettings.RANGE_ATTIVAZIONE && dist > 0.1) {
                Vector2 toPlayer = playerPos.copy().subtract(heartPos);
                return toPlayer.getNormalized().multiply(HeartSettings.VELOCITA_INSEGUIMENTO);
            }
            // Outside range: stand still
            return new Vector2();
        }
    }
}