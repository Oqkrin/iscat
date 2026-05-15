package uni.gaben.iscat.gamenex.universe.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Interpolator;

public class PlayerController {
    private PlayerModel player;
    private Shooter<PlayerModel> shooter;

    private double dashBufferTimer = 0;
    private boolean bufferedDashIsWASD = false;

    /**
     * Links this controller to a specific player instance.
     */
    public PlayerController(PlayerModel player) {
        this.player = player;
        shooter = new Shooter<>(player);
    }

    public PlayerModel getPlayer() {
        return player;
    }
    public void setPlayer(PlayerModel player) {
        this.player = player;
        shooter = new Shooter<>(player);
    }

    /**
     * Updates the current shooter (weapon).
     * Safe to set to null if the player has no weapon.
     */
    public void setShooter(Shooter<PlayerModel> shooter) {
        this.shooter = shooter;
    }

    public void processInput(InputManager input, double cameraX, double cameraY, double dt) {

        if (player == null) return;

        // --- 1. DIRECTIONAL INPUT ---
        double dx = 0, dy = 0;
        if (input.up)    dy -= 1;
        if (input.down)  dy += 1;
        if (input.left)  dx -= 1;
        if (input.right) dx += 1;

        // --- 2. MOVEMENT & ROTATION ---
        double currentAngle = player.getTransform().getRotationAngle();
        double nextAngle = currentAngle;

        if (!player.isInScatto()) {
            // Apply movement force
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
            }

            // Interpolate rotation towards mouse
            double mouseWorldX = (input.mouseX + cameraX) / UniverseSettings.SCALE;
            double mouseWorldY = (input.mouseY + cameraY) / UniverseSettings.SCALE;
            Vector2 playerPos = player.getTransform().getTranslation();

            double targetAngle = Math.atan2(mouseWorldY - playerPos.y, mouseWorldX - playerPos.x);
            double diff = targetAngle - currentAngle;

            // Normalization to prevent "spinning the long way around"
            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI)  diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);

            // Physic angular damping
            player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
        } else {
            // Lock rotation during dash
            player.setAngularVelocity(0);
        }

        // --- 3. DASH & SHOOTING ---
        handleDash(input, dx, dy, nextAngle, dt);
        handleShooting(input);
    }

    private void handleDash(InputManager input, double dx, double dy, double nextAngle, double dt) {
        if (dashBufferTimer > 0) dashBufferTimer -= dt;

        if (input.consumeDash()) {
            dashBufferTimer = 0.15;
            bufferedDashIsWASD = true;
        } else if (input.consumeDashMouse()) {
            dashBufferTimer = 0.15;
            bufferedDashIsWASD = false;
        }

        if (dashBufferTimer > 0 && player.isScattoDisponibile()) {
            double finalDashAngle = (bufferedDashIsWASD && (dx != 0 || dy != 0))
                    ? Math.atan2(dy, dx)
                    : nextAngle;

            player.executeScatto(finalDashAngle);
            dashBufferTimer = 0;
        }
    }

    private void handleShooting(InputManager input) {
        if (player == null || shooter == null) return;

        if (input.shooting && player.isSparoDisponibile()) {
            shooter.shoot();                    // spara
            player.startCooldownFuoco();        // attiva il cooldown
        }
    }
}