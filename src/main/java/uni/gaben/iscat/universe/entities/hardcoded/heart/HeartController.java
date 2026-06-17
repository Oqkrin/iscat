package uni.gaben.iscat.universe.entities.hardcoded.heart;


import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
public class HeartController extends Brain<HeartModel> {

    private final HeartModel heart;
    private boolean collected = false;

    public HeartController(HeartModel heart) {
        super(heart);
        this.heart = heart;

        // Collision callback
        this.heart.addOnCollision("consumable",otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !collected) {
                collected = true;
                double halfMaxLife = player.getMaxEndurance() / 2.0;
                player.restore(halfMaxLife);
                heart.setShouldRemove(true);
            }
        });

    }

    @Override
    public void update(UniverseModel universe, double dt) {
        AbstractPhysicalEntityModel player = universe.getPlayer();
        if (heart == null || heart.shouldRemove() || collected || player == null) return;
        super.update(universe, dt);
        if(player.getTransform().getTranslation().distance(heart.getTransform().getTranslation()) < 3) {
            setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 0));
        } else {
            setSteeringGoal(SteeringGoal.idle());
        }
    }

}