package uni.gaben.iscat.universe.entity.brain.actions;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.SummonPatternShooter;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class LifeBasedSummonAction extends Action {

    Set<SummonPatternShooter> summons ;

    public LifeBasedSummonAction(SummonPatternShooter... summons) {
        super("LifeBasedSummon", ActionCategory.SPECIAL, Collections.emptySet());
    }
    public LifeBasedSummonAction(String uuid, SummonPatternShooter... summons) {
        super(uuid, ActionCategory.SPECIAL, Collections.emptySet());
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        return false;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {

    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }
}
