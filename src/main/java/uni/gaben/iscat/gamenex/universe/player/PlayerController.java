package uni.gaben.iscat.gamenex.universe.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.controller.GamenexInputs;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileType;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Interpolator;
import uni.gaben.iscat.utils.Cooldown;

public class PlayerController {
    private PlayerModel player;
    private Shooter<PlayerModel> shooter;

    // Template riusabile: blueprint() ne crea una copia ad ogni sparo
    private final Projectile projectileTemplate;

    private final Cooldown dashBuffer = new Cooldown();
    private boolean bufferedDashIsWASD = false;

    public PlayerController(PlayerModel player) {
        this.player = player;
        this.shooter = new Shooter<>(player);

        projectileTemplate = new Projectile();
        projectileTemplate.setType(ProjectileType.PLAYER_BULLET);
    }

    /**
     * @param input         The clean input payload
     * @param viewportLeftX Explicit cameraModel.getViewportLeftX()
     * @param viewportTopY  Explicit cameraModel.getViewportTopY()
     * @param dt            Delta time seconds
     */
    public void processInput(GamenexInputs input, double viewportLeftX, double viewportTopY, double dt) {
        if (player == null) return;

        dashBuffer.update(dt);

        // --- 1. DIRECTIONAL INPUTS ---
        double dx = 0, dy = 0;
        if (input.up)    dy -= 1;
        if (input.down)  dy += 1;
        if (input.left)  dx -= 1;
        if (input.right) dx += 1;

        // --- 2. MOVEMENT & ROTATION SYSTEM ---
        double currentAngle = player.getTransform().getRotationAngle();
        double nextAngle = currentAngle;

        if (!player.isInScatto()) {
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
            }

            // 1. Convert player physical coordinates from meters to world pixels
            double playerWorldPxX = UU.mToPx(player.getTransform().getTranslationX());
            double playerWorldPxY = UU.mToPx(player.getTransform().getTranslationY());

            // 2. Map mouse coordinates to world pixels by adding the viewport top-left bounds
            double mouseWorldPxX = input.mouseX + viewportLeftX;
            double mouseWorldPxY = input.mouseY + viewportTopY;

            // 3. Compute absolute precise delta angle tracking
            double targetAngle = Math.atan2(mouseWorldPxY - playerWorldPxY, mouseWorldPxX - playerWorldPxX);
            double diff = targetAngle - currentAngle;

            // Shortest path arc interpolation wrap arounds
            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI)  diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);

            player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
        } else {
            player.setAngularVelocity(0);
        }

        // --- 3. EXECUTE DISCRETE ACTIONS ---
        handleDash(input, dx, dy, nextAngle);
        handleShooting(input);
    }

    private void handleDash(GamenexInputs input, double dx, double dy, double nextAngle) {
        if (input.consumeDash() || input.consumeDashMouse()) {
            dashBuffer.start(0.15);
            bufferedDashIsWASD = true;
        }

        if (dashBuffer.isCoolingDown() && player.isScattoDisponibile()) {
            double finalDashAngle = (bufferedDashIsWASD && (dx != 0 || dy != 0))
                    ? Math.atan2(dy, dx)
                    : nextAngle;

            player.executeScatto(finalDashAngle);
            dashBuffer.reset();
        }
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        // CRITICAL GUARD: Only initialize the shooter system if we have a real entity model
        if (player != null) {
            this.shooter = new Shooter<>(player);
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GamenexInputs input) {
        // Structural Guard: Ensure BOTH player and shooter instances are active and ready
        if (player == null || shooter == null) return;

        if (input.shooting && player.isSparoDisponibile()) {
            shooter.shoot(projectileTemplate);
            player.startCooldownFuoco();
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }

}