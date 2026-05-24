package uni.gaben.iscat.iscat_game.universe.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_screens.game.controller.GameInputs;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;
import java.util.function.Consumer;

public class PlayerController {
    private PlayerModel player;
    private Shooter<PlayerModel> shooter;

    private Projectile projectileTemplate;
    private final Cooldown dashBuffer = new Cooldown();
    private boolean bufferedDashIsWASD = false;

    public PlayerController(PlayerModel player) {
        setPlayer(player);
    }

    public void processInput(GameInputs input, double viewportLeftX, double viewportTopY, double dt) {
        if (player == null) return;

        dashBuffer.update(dt);

        double dx = 0, dy = 0;
        if (input.up) dy -= 1;
        if (input.down) dy += 1;
        if (input.left) dx -= 1;
        if (input.right) dx += 1;

        double currentAngle = player.getTransform().getRotationAngle();
        double nextAngle = currentAngle;

        if (!player.isInScatto()) {
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
            }

            double playerWorldPxX = UU.mToPx(player.getTransform().getTranslationX());
            double playerWorldPxY = UU.mToPx(player.getTransform().getTranslationY());
            double mouseWorldPxX = input.mouseX + viewportLeftX;
            double mouseWorldPxY = input.mouseY + viewportTopY;

            double targetAngle = Math.atan2(mouseWorldPxY - playerWorldPxY, mouseWorldPxX - playerWorldPxX);
            double diff = targetAngle - currentAngle;

            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI) diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);
            player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        handleDash(input, dx, dy, nextAngle);


    }

    private void handleDash(GameInputs input, double dx, double dy, double nextAngle) {
        if (input.consumeDash() || input.consumeDashMouse()) {
            dashBuffer.start(0.15);
            bufferedDashIsWASD = true;
        }

        if (dashBuffer.isCoolingDown() && player.isScattoDisponibile()) {
            double finalDashAngle = (bufferedDashIsWASD && (dx != 0 || dy != 0))
                    ? Math.atan2(dy, dx)
                    : nextAngle;

            player.getTransform().setRotation(finalDashAngle);
            player.setAngularVelocity(0);
            player.executeScatto(finalDashAngle);
            dashBuffer.reset();

            int randFart = new Random().nextInt(3) + 1;
            AudioManager.getInstance().playSFX("fart_alt" + randFart);
        }
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);
            this.projectileTemplate = new Projectile(ProjectileType.PLAYER_BULLET);
            this.player.lifeProperty().addListener((observable, oldValue, newValue) -> {
                if(oldValue.doubleValue() > newValue.doubleValue()) {
                AudioManager.getInstance().playSFX("hurt"); }
            });
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GameInputs input) {
        if (player == null || shooter == null) return;

        if (input.shooting && player.isSparoDisponibile()) {
            double angle = player.getTransform().getRotationAngle();
            double extraDamage = (player.getLevel() - 1) * 2.0;

            Consumer<Projectile> customizer = bullet -> {
                double newDamage = bullet.getLife() + extraDamage;
                bullet.setMaxLife(newDamage);
                bullet.setLife(newDamage);
            };

            int level = player.getLevel();

            if (level >= 9) {
                // PENTA SHOT: 5 bullets total. Max spread is 15°.
                // 2 side steps to reach the edge, so step = 15° / 2 = 7.5°
                double step = Math.toRadians(7.5);
                shooter.shoot(projectileTemplate, angle - step * 2, customizer); // -15.0° (Far Left)
                shooter.shoot(projectileTemplate, angle - step, customizer);     // -7.5°  (Mid Left)
                shooter.shoot(projectileTemplate, angle, customizer);             //  0.0°  (Center)
                shooter.shoot(projectileTemplate, angle + step, customizer);     //  +7.5°  (Mid Right)
                shooter.shoot(projectileTemplate, angle + step * 2, customizer); // +15.0° (Far Right)

            } else if (level >= 6) {
                // TRIPLE FAN SHOT: 3 bullets total. Max spread is 15°.
                // 1 side step to reach the edge, so step = 15°
                double step = Math.toRadians(15.0);
                shooter.shoot(projectileTemplate, angle - step, customizer);     // -15.0° (Left)
                shooter.shoot(projectileTemplate, angle, customizer);             //  0.0°  (Center)
                shooter.shoot(projectileTemplate, angle + step, customizer);     // +15.0° (Right)

            } else if (level >= 3) {
                // DUAL PARALLEL + CENTER: 3 bullets firing perfectly straight.
                // Spatially offset across the wings, keeping the center laser active.
                double cx = player.getTransform().getTranslationX();
                double cy = player.getTransform().getTranslationY();
                double dist = player.getHeightMeters() / 2.0;
                if (dist <= 0) dist = 0.2;

                double perpAngle = angle + Math.PI / 2.0;
                double lateralOffset = 0.15;

                Vector2 posLeft = new Vector2(
                        cx + Math.cos(angle) * dist + Math.cos(perpAngle) * lateralOffset,
                        cy + Math.sin(angle) * dist + Math.sin(perpAngle) * lateralOffset
                );
                Vector2 posRight = new Vector2(
                        cx + Math.cos(angle) * dist - Math.cos(perpAngle) * lateralOffset,
                        cy + Math.sin(angle) * dist - Math.sin(perpAngle) * lateralOffset
                );

                shooter.shoot(projectileTemplate, posLeft, angle, customizer);  // Left Wing (0° Angle)
                shooter.shoot(projectileTemplate, posRight, angle, customizer); // Right Wing (0° Angle)
                shooter.shoot(projectileTemplate, angle, customizer);           // Nose Cannon (0° Angle)

            } else {
                // STANDARD: 1 clean center-aimed bullet
                shooter.shoot(projectileTemplate, customizer);
            }

            player.startCooldownFuoco();
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }
}