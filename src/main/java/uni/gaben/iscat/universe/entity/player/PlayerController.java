package uni.gaben.iscat.universe.entity.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import uni.gaben.iscat.universe.entity.projectiles.shooters.*;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.function.Consumer;

public class PlayerController {

    private PlayerModel player;
    private Shooter<PlayerModel> shooter;
    private GameModel gameModel;          // for time‑scale control

    private final Cooldown dashBuffer = new Cooldown();
    private boolean bufferedDashIsWASD = false;
    private PatternShooter currentAttack;

    // Cache last level to avoid re‑assigning attack pattern every frame
    private int lastLevel = -1;

    public PlayerController(PlayerModel player) {
        setPlayer(player);
    }

    public void setGameModel(GameModel gm) {
        this.gameModel = gm;
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
            // Movement force
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                if (player.notStunned()) {
                    player.applyForce(dir.multiply(PlayerSettings.FORZA_SPINTA * player.getMass().getMass()));
                }
            }

            // Aiming (mouse)
            double screenCenterX = camera.getScreenWidth() / 2.0;
            double screenCenterY = camera.getScreenHeight() / 2.0;
            double zoom = camera.getZoom();
            double cx = camera.getX();
            double cy = camera.getY();

            double mouseWorldX = cx + (input.mouseX - screenCenterX) / zoom;
            double mouseWorldY = cy + (input.mouseY - screenCenterY) / zoom;

            double playerWorldX = UU.mToPx(player.getTransform().getTranslationX());
            double playerWorldY = UU.mToPx(player.getTransform().getTranslationY());

            double targetAngle = Math.atan2(mouseWorldY - playerWorldY, mouseWorldX - playerWorldX);
            double diff = targetAngle - currentAngle;
            while (diff < -Math.PI) diff += Math.PI * 2;
            while (diff > Math.PI) diff -= Math.PI * 2;

            nextAngle = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(15.0 * dt, 1.0));
            player.getTransform().setRotation(nextAngle);
            if (player.notStunned()) {
                player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
            }

            // Shooting (handles cooldown and level‑up attack pattern)
            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        // Dash & slow‑motion
        if (player.notStunned()) {
            handleDashAndSlowMotion(input, dx, dy, nextAngle);
        }
    }

    private void handleDashAndSlowMotion(GameInputsHandler input, double dx, double dy, double aimAngle) {
        // --- Slow‑motion from mouse dodge button (hold) ---
        if (gameModel != null) {
            gameModel.setTimeScale(input.slowMotionRequested ? 0.2 : 1.0);
        }

        // --- Keyboard dash (instant) ---
        if (input.dashKeyPressed && player.isScattoDisponibile()) {
            // Determine dash direction: movement keys if any, else aim angle
            double dashAngle;
            if (dx != 0 || dy != 0) {
                dashAngle = Math.atan2(dy, dx);
            } else {
                dashAngle = aimAngle;
            }
            player.getTransform().setRotation(dashAngle);
            player.executeScatto(dashAngle);
            input.dashKeyPressed = false; // consume
        }

        // (Optional) Mouse dash could also trigger a short slow‑motion after press.
        // Currently we use hold‑to‑slow – no extra action needed.
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);
            updateAttackPatternByLevel();          // initial assignment
            this.lastLevel = player.getLevel();    // cache

            this.player.lifeProperty().addListener((obs, old, newVal) -> {
                if (old.doubleValue() > newVal.doubleValue()) {
                    AudioManager.getInstance().playSFX("hurt");
                }
            });
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GameInputsHandler input) {
        if (player == null || shooter == null || currentAttack == null) return;

        // Only update attack pattern when level actually changes
        int currentLevel = player.getLevel();
        if (currentLevel != lastLevel) {
            updateAttackPatternByLevel();
            lastLevel = currentLevel;
        }

        if (input.shooting && player.isSparoDisponibile()) {
            double angle = player.getTransform().getRotationAngle();

            Consumer<ProjectileModel> customizer = bullet -> {
                double boostedLife = bullet.getLife() + player.getLevel();
                bullet.setMaxLifeDirect(boostedLife);
            };

            currentAttack.execute(shooter, ProjectileType.PLAYER_BULLET, angle, customizer);
            player.startCooldownFuoco();
        }
    }

    private void updateAttackPatternByLevel() {
        int level = player.getLevel();
        double baseCd = PlayerSettings.COOLDOWN_FUOCO_SEC;

        if (level >= 10) {
            this.currentAttack = new FigurePatternShooter(30, FigurePatternShooter.FigureType.STAR);
            player.setCooldownFuocoSec(baseCd * 0.8);
        } else if (level >= 7) {
            this.currentAttack = new SpreadPatternShooter(7, 45.0);
            player.setCooldownFuocoSec(baseCd * 0.85);
        } else if (level >= 4) {
            this.currentAttack = new SpreadPatternShooter(5, 30.0);
            player.setCooldownFuocoSec(baseCd * 0.9);
        } else if (level >= 2) {
            this.currentAttack = new SpreadPatternShooter(3, 15.0);
            player.setCooldownFuocoSec(baseCd * 0.95);
        } else {
            this.currentAttack = new SingleShotPatternShooter();
            player.setCooldownFuocoSec(baseCd);
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }
}