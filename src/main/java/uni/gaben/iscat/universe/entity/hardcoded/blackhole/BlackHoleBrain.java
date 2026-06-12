package uni.gaben.iscat.universe.entity.hardcoded.blackhole;


import org.dyn4j.world.DetectFilter;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.abilities.GravityPullAbility;

public class BlackHoleBrain extends Brain<BlackHoleModel> {
    public BlackHoleBrain(BlackHoleModel entity) {
        super(entity);
        addAction("gravity",
                new GravityPullAbility(Target.neighbours(entity, entity.getRadius().m().get()*10, new DetectFilter<>(true, true, null)))
        );

        entity.getRadius().m().addListener(
                (_, _, newRadius)
                        -> replaceAction("gravity",
                        new GravityPullAbility(Target.neighbours(entity, (double)newRadius*10, new DetectFilter<>(true, true, null)))
                ));
    }
}
