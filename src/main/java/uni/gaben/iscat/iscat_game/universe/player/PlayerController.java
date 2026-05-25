package uni.gaben.iscat.iscat_game.universe.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.universe.attacks2.*;
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
    private AttackPattern currentAttack;

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

            this.currentAttack = new SingleShotAttack();

            this.player.lifeProperty().addListener((observable, oldValue, newValue) -> {
                if(oldValue.doubleValue() > newValue.doubleValue()) {
                    AudioManager.getInstance().playSFX("hurt");
                }
            });
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GameInputs input) {
        if (player == null || shooter == null || currentAttack == null) return;

        if (input.shooting && player.isSparoDisponibile()) {
            double angle = player.getTransform().getRotationAngle();

            Consumer<Projectile> customized = bullet -> {
                bullet.setMaxLife(bullet.getLife() + player.getLevel());
                bullet.setLife(bullet.getMaxLife());
            };

            updateAttackPatternByLevel();

            currentAttack.execute(shooter, projectileTemplate, angle, customized);

            player.startCooldownFuoco();
        }
    }

    private void updateAttackPatternByLevel() {
        int level = player.getLevel();

        if (level >= 9) {
            this.currentAttack = new SpreadAttack(5, 30.0);
            player.setCooldownFuocoSec(PlayerSettings.COOLDOWN_FUOCO_SEC * 0.4);
        } else if (level >= 6) {
            this.currentAttack = new SpreadAttack(3, 30.0);
            player.setCooldownFuocoSec(PlayerSettings.COOLDOWN_FUOCO_SEC * 0.6);
        } else if (level >= 3) {
            this.currentAttack = new ParallelLineAttack(3, 15.0);
            player.setCooldownFuocoSec(PlayerSettings.COOLDOWN_FUOCO_SEC * 0.8);
        } else {
            this.currentAttack = new MultiDirectionAttack(16, 100, new SpreadAttack(7, 10));
            player.setCooldownFuocoSec(PlayerSettings.COOLDOWN_FUOCO_SEC);
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }
}