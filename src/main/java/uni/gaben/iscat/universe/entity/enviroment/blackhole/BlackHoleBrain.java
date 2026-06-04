package uni.gaben.iscat.universe.entity.enviroment.blackhole;


import org.dyn4j.world.DetectFilter;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.actions.GravityPullAction;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;

public class BlackHoleBrain extends Brain<BlackHoleModel> {
    public BlackHoleBrain(BlackHoleModel entity) {
        super(entity, SteeringGoal.idle(), 0, 0, 0, 0);
        addAction("gravity",
                new GravityPullAction(Target.neighbours(entity, entity.getRadius().m().get()*10, new DetectFilter<>(true, true, null)))
        );

        entity.getRadius().m().addListener(
                (_, _, newRadius)
                        -> replaceAction("gravity",
                        new GravityPullAction(Target.neighbours(entity, (double)newRadius*10, new DetectFilter<>(true, true, null)))
                ));
    }
}
