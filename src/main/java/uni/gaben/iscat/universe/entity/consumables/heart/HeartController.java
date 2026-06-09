package uni.gaben.iscat.universe.entity.consumables.heart;


import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.player.PlayerModelAbstract;
public class HeartController extends Brain<HeartModelAbstract> {

    private final HeartModelAbstract heart;
    private boolean collected = false;

    public HeartController(HeartModelAbstract heart) {
        super(heart);
        this.heart = heart;

        // Collision callback
        this.heart.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModelAbstract player && !collected) {
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
        AbstractEntityModel player = universe.getPlayer();
        if (heart == null || heart.shouldRemove() || collected || player == null) return;
        super.update(universe, dt);
        if(player.getTransform().getTranslation().distance(heart.getTransform().getTranslation()) < 3) {
            setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 0));
        } else {
            setSteeringGoal(SteeringGoal.idle());
        }
    }

}