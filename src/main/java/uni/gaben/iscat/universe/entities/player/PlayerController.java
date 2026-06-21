package uni.gaben.iscat.universe.entities.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityRecordParser;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.Shooter;
import uni.gaben.iscat.universe.entities.shooters.*;
import uni.gaben.iscat.utils.audio.AudioManager;
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
                    double force = player.getEntityRecord().player().baseThrustForce();
                    player.applyForce(dir.multiply(force * player.getMass().getMass()));
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
            handleDashAndSlowMotion(input, nextAngle, dx, dy, dt);
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

    private void handleDashAndSlowMotion(GameInputsHandler input, double aimAngle, double dx, double dy, double dt) {
        // --- Slow‑motion from mouse dodge button (hold) ---
        if (gameModel != null) {
            if (input.slowMotionRequested && player.getTimeGauge() > 0) {
                gameModel.setTimeScale(0.2);
                player.decreaseTimeGauge(dt * 30.0); // Consume 30 gauge per second
                player.setTemporaryTerminalVelocity(player.getTerminalVelocity() * 2.0);
            } else {
                gameModel.setTimeScale(1.0);
                player.restoreTerminalVelocity();
            }
        }

        // --- Keyboard dash (instant) – always in the movement direction ---
        if (input.dashKeyPressed && player.canDash()) {
            double dashAngle;
            if (dx != 0 || dy != 0) {
                dashAngle = Math.atan2(dy, dx);          // direction of movement
            } else {
                dashAngle = aimAngle;                     // no movement → use mouse aim
            }
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

        int currentLevel = player.getLevel();
        if (currentLevel != lastLevel) {
            updateAttackPatternByLevel();
            lastLevel = currentLevel;
        }

        if (input.shooting && player.isSparoDisponibile()) {
            double angle = player.getTransform().getRotationAngle();

            Consumer<ProjectileModel> customizer = bullet -> {
                // Applichiamo il tipo (che imposta la fisica base)
                bullet.setType(ProjectileType.PLAYER_BULLET);

                // Calcoliamo il danno dinamico basato sul livello
                double dynamicDamage = player.getProjectileDamage();

                // Forziamo l'energia calcolata sul proiettile
                bullet.setEnergyDirect(dynamicDamage);

                // Se la logica precedente modificava ulteriormente la durata/vita del proiettile:
                double boostedLife = bullet.getEndurance() + player.getLevel();
                bullet.setMaxEnduranceDirect(boostedLife);
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