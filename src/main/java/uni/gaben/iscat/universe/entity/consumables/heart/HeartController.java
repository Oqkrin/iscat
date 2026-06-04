package uni.gaben.iscat.universe.entity.consumables.heart;


import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
public class HeartController extends Brain<HeartModel> {

    private final HeartModel heart;
    private boolean collected = false;

    public HeartController(HeartModel heart) {
        super(heart, MovementGoal.idle() ,heart.getBaseAccelerationPerTick(), heart.getTerminalVelocity(), 0.0, 0);
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

    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (heart == null || heart.shouldRemove() || collected) return;
        super.update(universe, dt);
        if(universe.getPlayer().getTransform().getTranslation().distance(heart.getTransform().getTranslation()) < 3) {
            setMovementGoal(MovementGoal.pursuit(Target.ofPlayer()));
        } else {
            setMovementGoal(MovementGoal.idle());
        }
    }

}