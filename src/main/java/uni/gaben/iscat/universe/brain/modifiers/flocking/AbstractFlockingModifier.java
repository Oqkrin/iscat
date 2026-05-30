package uni.gaben.iscat.universe.brain.modifiers.flocking;

import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.modifiers.MovementModifier;

public abstract class AbstractFlockingModifier implements MovementModifier {
    protected final Target flock;
    protected final double range;
    protected final double multiplier;

    protected AbstractFlockingModifier(Target flock, double range, double multiplier) {
        this.flock = flock;
        this.range = range;
        this.multiplier = multiplier;
    }
}