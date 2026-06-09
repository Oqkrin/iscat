package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.AbstractLivingModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Collections;
import java.util.function.Predicate;

public class MeleeAbility<T extends AbstractEntityModel> extends Ability {

    Cooldown meleeCooldown = new Cooldown();
    double damage;
    T entity;
    Predicate<Body> targets;

    public MeleeAbility(String name, T entity, double cooldown, double damage, Predicate<Body> targets) {
        super(name, AbilityCategory.SPECIAL, Collections.emptySet());
        meleeCooldown.setDefaultDuration(cooldown);
        this.entity = entity;
        this.damage = damage;
        this.targets = targets;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        return meleeCooldown.isReady();
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        entity.setOnCollision(
                other -> {
                    if(meleeCooldown.isReady() && targets.test(other) && other instanceof AbstractLivingModel l) {
                        l.deltaToLife(-damage);
                    }
                }
        );
        meleeCooldown.start();
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        meleeCooldown.update(dt);
        if(meleeCooldown.isCoolingDown() && entity.hasCollision()) {
            entity.setOnCollision(null);
        }
        return true;
    }
}
