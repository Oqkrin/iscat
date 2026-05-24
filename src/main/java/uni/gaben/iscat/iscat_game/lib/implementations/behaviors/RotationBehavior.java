package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.utils.Interpolator;

public class RotationBehavior implements AiBehavior {

    private double targetAngle = 0.0;
    private double timer = 0.0;
    private int stepCount = 0;

    private final double interval;
    private final double stepRadians;
    private final double rotationSpeed;
    private final int stepsBeforeReset;

    public RotationBehavior(double intervalSeconds, double stepDegrees, double rotationSpeed, int stepsBeforeReset) {
        this.interval = intervalSeconds;
        this.stepRadians = Math.toRadians(stepDegrees);
        this.rotationSpeed = rotationSpeed;
        this.stepsBeforeReset = stepsBeforeReset;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return -1.0; // Parallelo, sempre eseguito
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        timer += dt;
        if (timer >= interval || stepCount >= stepsBeforeReset) {
            timer -= interval;
            if (stepCount >= stepsBeforeReset) stepCount = 0;
            targetAngle += stepRadians;
            if (targetAngle >= Math.PI * 2.0) targetAngle -= Math.PI * 2.0;
            stepCount++;
        }

        // Lerp verso l'angolo target
        npc.setAngularVelocity(0.0);
        double current = npc.getTransform().getRotationAngle();
        double diff = targetAngle - current;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        double next = Interpolator.lerp(current, current + diff, Math.min(rotationSpeed * dt, 1.0));
        npc.getTransform().setRotation(next);
    }
}