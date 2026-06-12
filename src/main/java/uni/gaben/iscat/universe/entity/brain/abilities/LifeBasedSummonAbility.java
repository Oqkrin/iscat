package uni.gaben.iscat.universe.entity.brain.abilities;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.shooters.SummonPatternShooter;

import java.util.Collections;
import java.util.Set;

public class LifeBasedSummonAbility extends Ability {

    Set<SummonPatternShooter> summons ;

    public LifeBasedSummonAbility(SummonPatternShooter... summons) {
        super("LifeBasedSummon", AbilityCategory.SPECIAL, Collections.emptySet());
    }
    public LifeBasedSummonAbility(String uuid, SummonPatternShooter... summons) {
        super(uuid, AbilityCategory.SPECIAL, Collections.emptySet());
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
