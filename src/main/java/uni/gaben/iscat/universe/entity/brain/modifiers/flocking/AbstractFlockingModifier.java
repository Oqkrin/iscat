package uni.gaben.iscat.universe.entity.brain.modifiers.flocking;

import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.modifiers.MovementModifier;

public abstract class AbstractFlockingModifier implements MovementModifier {
    protected final Target flock;
    protected final double multiplier;

    protected AbstractFlockingModifier(Target flock, double multiplier) {
        this.flock = flock;
        this.multiplier = multiplier;
    }
}