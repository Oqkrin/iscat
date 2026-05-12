package uni.gaben.iscat.gamenex.universe.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.Interpolator;

public class PlayerController {

    private double dashBufferTimer = 0;
    private boolean bufferedDashIsWASD = false;

    public void processInput(PlayerModel player, InputManager input, double cameraX, double cameraY, double dt) {
        // --- 1. COORDINATE E DIREZIONI ---
        double dx = 0, dy = 0;
        if (input.up) dy -= 1;
        if (input.down) dy += 1;
        if (input.left) dx -= 1;
        if (input.right) dx += 1;

        // --- 2. MOVIMENTO E ROTAZIONE ---
        double currentAngle = player.getTransform().getRotationAngle();
        double nextAngle = currentAngle;

        if (!player.isInScatto()) {
            // Movimento normale
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
            }

            // Rotazione fluida verso il mouse
            double mouseWorldX = (input.mouseX + cameraX) / UniverseSettings.SCALE;
            double mouseWorldY = (input.mouseY + cameraY) / UniverseSettings.SCALE;
            Vector2 playerPos = player.getTransform().getTranslation();
            double targetAngle = Math.atan2(mouseWorldY - playerPos.y, mouseWorldX - playerPos.x);

            double diff = targetAngle - currentAngle;
            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI) diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);

            // Smorzamento rotazione fisica
            player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
        } else {
            // Lock rotazione durante il dash
            player.setAngularVelocity(0);
        }

        // --- 3. GESTIONE BUFFER DASH ---
        if (dashBufferTimer > 0) dashBufferTimer -= dt;

        if (input.consumeDash()) {
            dashBufferTimer = 0.15;
            bufferedDashIsWASD = true;
        } else if (input.consumeDashMouse()) {
            dashBufferTimer = 0.15;
            bufferedDashIsWASD = false;
        }

        // --- 4. ESECUZIONE DASH ---
        if (dashBufferTimer > 0 && player.isScattoDisponibile()) {
            double finalDashAngle;
            if (bufferedDashIsWASD && (dx != 0 || dy != 0)) {
                finalDashAngle = Math.atan2(dy, dx);
            } else {
                finalDashAngle = nextAngle; // Verso il mouse
            }
            player.executeScatto(finalDashAngle);
            dashBufferTimer = 0;
        }

        // --- 5. SPARO ---
        if (input.shooting && player.isSparoDisponibile()) {
            player.startCooldownFuoco();
        }
    }
}