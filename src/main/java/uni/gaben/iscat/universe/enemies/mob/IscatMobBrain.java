package uni.gaben.iscat.universe.enemies.mob;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class IscatMobBrain extends Brain<IscatMobModel> {

    public IscatMobBrain(IscatMobModel entity) {
        super(entity, defaultGoal, ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);
    }
}