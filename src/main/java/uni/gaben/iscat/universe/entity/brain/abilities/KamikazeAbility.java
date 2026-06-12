package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.interfaces.Alterable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class KamikazeAbility extends Ability {

    public KamikazeAbility(AbstractEntityModel self, double damage, Predicate<Body> targets) {
        this(self, damage ,targets ,"Kamikaze",AbilityCategory.SPECIAL, Collections.emptySet());

    }
    protected KamikazeAbility(AbstractEntityModel self, double damage, Predicate<Body> targets ,String name, AbilityCategory category, Set<AbilityCategory> blockedCategories) {
        super(name, category, blockedCategories);
        self.addOnCollision(name, entityModel -> {
            if(targets.test(entityModel)) {
                if(entityModel instanceof Alterable al) {
                    al.damage(damage);
                }
                self.setShouldRemove(true);
            }
        });
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        return false;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {}

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }
}
