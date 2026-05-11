package uni.gaben.iscat.gamenex.universe.player.controller;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerSettings;

public class PlayerController {

    public void processInput(PlayerModel player, InputManager input, double cameraX, double cameraY) {
        double dx = 0;
        double dy = 0;
        if (input.up) dy -= 1;
        if (input.down) dy += 1;
        if (input.left) dx -= 1;
        if (input.right) dx += 1;

        if (dx != 0 || dy != 0) {
            Vector2 dir = new Vector2(dx, dy);
            dir.normalize();
            double mass = player.getMass().getMass();
            player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * mass));
        }

        // Angle towards mouse
        // Convert screen coordinates to world coordinates by dividing by SCALE
        double targetWorldX = (input.mouseX + cameraX) / UniverseSettings.SCALE;
        double targetWorldY = (input.mouseY + cameraY) / UniverseSettings.SCALE;
        
        double cx = player.getTransform().getTranslationX();
        double cy = player.getTransform().getTranslationY();
        double targetAngleRad = Math.atan2(targetWorldY - cy, targetWorldX - cx);
        
        double currentAngle = player.getTransform().getRotationAngle();
        double diff = targetAngleRad - currentAngle;
        
        // Normalize difference to [-PI, PI] for shortest path rotation
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;
        
        // Apply smoother interpolation towards the target angle
        double newAngle = uni.gaben.iscat.utils.Interpolator.smootherStep(currentAngle, currentAngle + diff, 0.3);
        player.getTransform().setRotation(newAngle);

        if (input.consumeDash() && player.isScattoDisponibile()) {
            player.executeScatto(targetAngleRad);
        }

        if (input.shooting && player.isSparoDisponibile()) {
            // TODO: implement projectile spawning inside SpaceModel or SpaceController
            player.startCooldownFuoco();
        }
    }
}
