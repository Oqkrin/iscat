package uni.gaben.iscat.universe.entity.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.PatternShooter;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.*;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;
import java.util.function.Consumer;

public class PlayerController {
    private PlayerModel player;
    private Shooter<PlayerModel> shooter;

    private final Cooldown dashBuffer = new Cooldown();
    private boolean bufferedDashIsWASD = false;
    private PatternShooter currentAttack;

    public PlayerController(PlayerModel player) {
        setPlayer(player);
    }

    public void processInput(GameInputsHandler input, CameraModel camera, double dt) {
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
                if (player.notStunned()) player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
            }


        double screenCenterX = camera.getScreenWidth() / 2.0;
        double screenCenterY = camera.getScreenHeight() / 2.0;
        double zoom = camera.getZoom();
        double cx = camera.getX();
        double cy = camera.getY();

// Convert mouse screen coordinates to world pixels
        double mouseWorldX = cx + (input.mouseX - screenCenterX) / zoom;
        double mouseWorldY = cy + (input.mouseY - screenCenterY) / zoom;

// Player position in world pixels
        double playerWorldX = UU.mToPx(player.getTransform().getTranslationX());
        double playerWorldY = UU.mToPx(player.getTransform().getTranslationY());

// Aim angle
        double targetAngle = Math.atan2(mouseWorldY - playerWorldY, mouseWorldX - playerWorldX);

            double diff = targetAngle - currentAngle;

            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI) diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);
            if (player.notStunned()) player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        if (player.notStunned()) handleDash(input, dx, dy, nextAngle);
    }

    private void handleDash(
            GameInputsHandler input,
            double dx,
            double dy,
            double nextAngle
    ) {

        if (input.consumeDash()) {

            dashBuffer.start(0.15);

            // keyboard dash = movement-relative
            bufferedDashIsWASD = true;
        }

        if (input.consumeDashMouse()) {

            dashBuffer.start(0.15);

            // mouse dash = aim-relative
            bufferedDashIsWASD = false;
        }

        if (dashBuffer.isCoolingDown() &&
                player.isScattoDisponibile()) {

            double finalDashAngle;

            if (bufferedDashIsWASD &&
                    (dx != 0 || dy != 0)) {

                finalDashAngle = Math.atan2(dy, dx);

            } else {

                finalDashAngle = nextAngle;
            }

            player.getTransform().setRotation(finalDashAngle);

            player.setAngularVelocity(0);

            player.executeScatto(finalDashAngle);

            dashBuffer.reset();

            int randFart =
                    new Random().nextInt(3) + 1;

            AudioManager.getInstance()
                    .playSFX("fart_alt" + randFart);
        }
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);

            this.currentAttack = new SingleShotPatternShooter();

            this.player.lifeProperty().addListener((observable, oldValue, newValue) -> {
                if(oldValue.doubleValue() > newValue.doubleValue()) {
                    AudioManager.getInstance().playSFX("hurt");
                }
            });
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GameInputsHandler input) {
        if (player == null || shooter == null || currentAttack == null) return;

        if (input.shooting && player.isSparoDisponibile()) {
            double angle = player.getTransform().getRotationAngle();

            Consumer<Projectile> customized = bullet -> {
                double boostedLife = bullet.getLife() + player.getLevel();
                bullet.setMaxLifeDirect(boostedLife);
            };

            updateAttackPatternByLevel();

            currentAttack.execute(shooter, ProjectileType.PLAYER_BULLET, angle, customized);

            player.startCooldownFuoco();
        }
    }

    private void updateAttackPatternByLevel() {
        int level = player.getLevel();

        // Reset base per evitare stati inconsistenti
        double baseCd = PlayerSettings.COOLDOWN_FUOCO_SEC;

        if (level >= 10) {
            this.currentAttack = new FigurePatternShooter(30, FigurePatternShooter.FigureType.STAR);
            player.setCooldownFuocoSec(baseCd * 0.8);

        }
        else if (level >= 7) {
            this.currentAttack = new SpreadPatternShooter(7, 45.0);
            player.setCooldownFuocoSec(baseCd * 0.85);
        }
        else if (level >= 4) {
            this.currentAttack = new SpreadPatternShooter(5, 30.0);
            player.setCooldownFuocoSec(baseCd * 0.9);
        }
        else if (level >= 2) {
            this.currentAttack = new SpreadPatternShooter(3, 15.0);
            player.setCooldownFuocoSec(baseCd * 0.95);
        }
        else {
            this.currentAttack = new SingleShotPatternShooter();
            player.setCooldownFuocoSec(baseCd);
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }
}