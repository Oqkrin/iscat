package uni.gaben.iscat.universe.enviroment.blackhole;


import org.dyn4j.world.DetectFilter;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.actions.GravityPullAction;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;

public class BlackHoleBrain extends Brain<BlackHoleModel> {
    public BlackHoleBrain(BlackHoleModel entity) {
        super(entity, MovementGoal.idle(), 0, 0, 0);
        addAction("gravity",
                new GravityPullAction(Target.neighbours(entity, entity.getRadius().m().get()*10, new DetectFilter<>(true, true, null)))
        );

        entity.getRadius().m().addListener((observable, oldValue, newRange) -> {
            removeAction("gravity");

                    addAction("gravity",
                            new GravityPullAction(Target.neighbours(entity, (Double) newRange*10, new DetectFilter<>(true, true, null)))
                    );

                }
        );

    }
}
