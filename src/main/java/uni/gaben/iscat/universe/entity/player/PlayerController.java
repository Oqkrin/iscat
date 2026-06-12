package uni.gaben.iscat.universe.entity.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.universe.entity.modules.MovementModule;
import uni.gaben.iscat.universe.entity.modules.StateModule;
import uni.gaben.iscat.universe.entity.modules.XpModule;

import uni.gaben.iscat.universe.entity.projectiles.shooters.Shooter;
import uni.gaben.iscat.universe.entity.projectiles.shooters.*;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.function.Consumer;

public class PlayerController {

    private GameEntity player;
    private Shooter<GameEntity> shooter;
    private GameModel gameModel;          

    private final Cooldown dashBuffer = new Cooldown();
    private final Cooldown fireCooldown = new Cooldown();
    
    private PatternShooter currentAttack;
    private int lastLevel = -1;
    private double lastHealth = -1;

    public PlayerController(GameEntity player) {
        setPlayer(player);
    }

    public void setGameModel(GameModel gm) {
        this.gameModel = gm;
    }

    public void processInput(GameInputsHandler input, CameraModel camera, double dt) {
        dashBuffer.update(dt);
        fireCooldown.update(dt);
        
        if (player == null || player.shouldRemove()) return;
        
        MovementModule mov = player.hasModule(MovementModule.class) ? player.getModule(MovementModule.class) : null;
        StateModule state = player.hasModule(StateModule.class) ? player.getModule(StateModule.class) : null;

        boolean isStunned = state != null && state.isStunned();
        boolean isDashing = mov != null && mov.isDashing();

        // Manual health listener check
        if (player.hasModule(EnduranceModule.class)) {
            double currentHealth = player.getModule(EnduranceModule.class).getEndurance();
            if (lastHealth >= 0 && currentHealth < lastHealth) {
                AudioManager.getInstance().playSFX("hurt");
            }
            lastHealth = currentHealth;
        }

        double dx = 0, dy = 0;
        if (input.up) dy -= 1;
        if (input.down) dy += 1;
        if (input.left) dx -= 1;
        if (input.right) dx += 1;

        double currentAngle = player.getTransform().getRotationAngle();
        double nextAngle = currentAngle;

        if (!isDashing) {
            // Movement force
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                if (!isStunned) {
                    double maxForce = mov != null ? mov.getAcceleration() : 30.0;
                    player.applyForce(dir.multiply(maxForce * player.getMass().getMass()));
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
            if (!isStunned) {
                player.setAngularVelocity(Interpolator.lerp(player.getAngularVelocity(), 0, Math.min(20.0 * dt, 1.0)));
            }

            // Shooting 
            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        // Dash & slow-motion
        if (!isStunned) {
            handleDashAndSlowMotion(input, dx, dy, nextAngle, mov);
        }
    }

    private void handleDashAndSlowMotion(GameInputsHandler input, double dx, double dy, double aimAngle, MovementModule mov) {
        if (gameModel != null) {
            gameModel.setTimeScale(input.slowMotionRequested ? 0.2 : 1.0);
        }

        if (input.dashKeyPressed && mov != null && mov.canDash()) {
            double dashAngle = (dx != 0 || dy != 0) ? Math.atan2(dy, dx) : aimAngle;
            player.getTransform().setRotation(dashAngle);
            mov.dashTowards(dashAngle);
            input.dashKeyPressed = false; 
        }
    }

    public void setPlayer(GameEntity player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);
            if (player.hasModule(EnduranceModule.class)) {
                this.lastHealth = player.getModule(EnduranceModule.class).getEndurance();
            }
            updateAttackPatternByLevel();          
        } else {
            this.shooter = null;
        }
    }

    private void handleShooting(GameInputsHandler input) {
        if (player == null || shooter == null || currentAttack == null) return;

        int currentLevel = player.hasModule(XpModule.class) ? player.getModule(XpModule.class).getLevel() : 1;
        if (currentLevel != lastLevel) {
            updateAttackPatternByLevel();
            lastLevel = currentLevel;
        }

        if (input.shooting && fireCooldown.isReady()) {
            double angle = player.getTransform().getRotationAngle();

            Consumer<GameEntity> customizer = bullet -> {
                double boostedLife = bullet.getEndurance() + currentLevel;
                bullet.setEndurance(boostedLife);
            };

            currentAttack.execute(shooter, "player_bullet", angle, customizer);
            fireCooldown.start(player.getRecord().player() != null ? player.getRecord().player().baseCooldownSec() : 0.5);
        }
    }

    private void updateAttackPatternByLevel() {
        int level = player.hasModule(XpModule.class) ? player.getModule(XpModule.class).getLevel() : 1;
        double baseCd = player.getRecord().player() != null ? player.getRecord().player().baseCooldownSec() : 0.5;

        if (level >= 10) {
            this.currentAttack = new FigurePatternShooter(30, FigurePatternShooter.FigureType.STAR);
            fireCooldown.setDefaultDuration(baseCd * 0.8);
        } else if (level >= 7) {
            this.currentAttack = new SpreadPatternShooter(7, 45.0);
            fireCooldown.setDefaultDuration(baseCd * 0.85);
        } else if (level >= 4) {
            this.currentAttack = new SpreadPatternShooter(5, 30.0);
            fireCooldown.setDefaultDuration(baseCd * 0.9);
        } else if (level >= 2) {
            this.currentAttack = new SpreadPatternShooter(3, 15.0);
            fireCooldown.setDefaultDuration(baseCd * 0.95);
        } else {
            this.currentAttack = new SingleShotPatternShooter();
            fireCooldown.setDefaultDuration(baseCd);
        }
    }

    public GameEntity getPlayer() {
        return player;
    }
}
