package uni.gaben.iscat.universe.entities.hardcoded.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityRecordParser;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.shooters.Shooter;
import uni.gaben.iscat.universe.entities.shooters.*;
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
    private Pattern currentAttack;

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

        if (!player.isDashing()) {
            // Movement force
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                if (!player.isStunned()) {
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
            if (!player.isStunned()) {
                player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
            }

            // Shooting (handles cooldown and level‑up attack pattern)
            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        // Dash & slow‑motion
        if (!player.isStunned()) {
            handleDashAndSlowMotion(input, nextAngle);
        }

        // Quick dash (double‑tap)
        if (!player.isStunned() && !player.isDashing()) {
            javafx.geometry.Point2D quickDir = input.consumeQuickDash();
            if (quickDir != null) {
                double angle = Math.atan2(quickDir.getY(), quickDir.getX());
                player.quickDash(angle);
            }
        }
    }

    private void handleDashAndSlowMotion(GameInputsHandler input, double aimAngle) {
        // --- Slow‑motion from mouse dodge button (hold) ---
        if (gameModel != null) {
            gameModel.setTimeScale(input.slowMotionRequested ? 0.2 : 1.0);
        }

        // --- Keyboard dash (instant) – always toward mouse aim ---
        if (input.dashKeyPressed && player.canDash()) {
            // Dash always in the direction the player is facing (mouse aim)
            double dashAngle = aimAngle;
            player.getTransform().setRotation(dashAngle);
            player.dashTowards(dashAngle);
            input.dashKeyPressed = false; // consume
        }
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);
            updateAttackPatternByLevel();          // initial assignment
            this.lastLevel = player.getLevel();    // cache

            this.player.enduranceProperty().addListener((obs, old, newVal) -> {
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
                double boostedLife = bullet.getEndurance() + player.getLevel();
                bullet.setMaxLifeDirect(boostedLife);
                bullet.setType(ProjectileType.PLAYER_BULLET);
            };

            currentAttack.execute(shooter, ProjectileType.PLAYER_BULLET, angle, customizer);
            player.startCooldownFuoco();
        }
    }

    private void updateAttackPatternByLevel() {
        int level = player.getLevel();
        EntityRecord data = player.getEntityRecord();

        if (data == null || data.player() == null) {
            // Fallback di sicurezza se il JSON non è caricato
            this.currentAttack = new SingleShotPattern();
            return;
        }

        for (EntityRecord.LevelAbility ability : data.player().levelAbilities()) {
            if (level >= ability.minLevel()) {

                this.currentAttack = EntityRecordParser.createPattern(ability.pattern());
                player.setCooldownFuocoSec(ability.cooldownSec());
                break;
            }
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }
}