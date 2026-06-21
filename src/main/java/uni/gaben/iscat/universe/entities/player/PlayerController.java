package uni.gaben.iscat.universe.entities.player;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordParser;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import uni.gaben.iscat.universe.entities.shooters.*;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.function.Consumer;

/**
 * Gestisce gli input della tastiera e del mouse per controllare il movimento,
 * la rotazione, lo sparo e le abilità speciali del giocatore.
 */
public class PlayerController {

    private PlayerModel player;
    private Shooter<PlayerModel> shooter;
    private GameModel gameModel;

    private final Cooldown dashBuffer = new Cooldown();
    private Pattern currentAttack;
    private int lastLevel = -1;

    public PlayerController(PlayerModel player) {
        setPlayer(player);
    }

    public void setGameModel(GameModel gm) {
        this.gameModel = gm;
    }

    /**
     * Elabora gli input correnti per muovere il personaggio, ruotarlo verso il mouse
     * e gestire lo sparo o i dash fisici.
     */
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
            if (dx != 0 || dy != 0) {
                Vector2 dir = new Vector2(dx, dy).getNormalized();
                if (!player.isStunned()) {
                    double force = player.getEntityRecord().player().baseThrustForce();
                    player.applyForce(dir.multiply(force * player.getMass().getMass()));
                }
            }

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

            handleShooting(input);
        } else {
            player.setAngularVelocity(0);
        }

        if (!player.isStunned()) {
            handleDashAndSlowMotion(input, nextAngle, dx, dy, dt);
        }

        if (!player.isStunned() && !player.isDashing()) {
            javafx.geometry.Point2D quickDir = input.consumeQuickDash();
            if (quickDir != null) {
                double angle = Math.atan2(quickDir.getY(), quickDir.getX());
                player.quickDash(angle);
            }
        }
    }

    /**
     * Gestisce l'attivazione dello scatto (Dash) e della modalità rallentatore (Slow-motion).
     */
    private void handleDashAndSlowMotion(GameInputsHandler input, double aimAngle, double dx, double dy, double dt) {
        if (gameModel != null) {
            if (input.slowMotionRequested && player.getTimeGauge() > 0) {
                gameModel.setTimeScale(0.2);
                player.decreaseTimeGauge(dt * 30.0);
                player.setTemporaryTerminalVelocity(player.getTerminalVelocity() * 2.0);
            } else {
                gameModel.setTimeScale(1.0);
                player.restoreTerminalVelocity();
            }
        }

        if (input.dashKeyPressed && player.canDash()) {
            double dashAngle;
            if (dx != 0 || dy != 0) {
                dashAngle = Math.atan2(dy, dx);
            } else {
                dashAngle = aimAngle;
            }
            player.getTransform().setRotation(dashAngle);
            player.dashTowards(dashAngle);
            input.dashKeyPressed = false;
        }
    }

    /**
     * Associa il modello del giocatore al controller e registra il listener per l'audio dei danni.
     */
    public void setPlayer(PlayerModel player) {
        this.player = player;
        if (player != null) {
            this.shooter = new Shooter<>(player);
            updateAttackPatternByLevel();
            this.lastLevel = player.getLevel();

            this.player.enduranceProperty().addListener((obs, old, newVal) -> {
                if (old.doubleValue() > newVal.doubleValue()) {
                    AudioManager.getInstance().playSFX("hurt");
                }
            });
        } else {
            this.shooter = null;
        }
    }

    /**
     * Controlla i requisiti di sparo, aggiorna il pattern se il livello è cambiato ed esegue l'attacco.
     */
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
                bullet.setType(ProjectileType.PLAYER_BULLET);
                double dynamicDamage = player.getProjectileDamage();
                bullet.setEnergyDirect(dynamicDamage);
                double boostedLife = bullet.getEndurance() + player.getLevel();
                bullet.setMaxEnduranceDirect(boostedLife);
            };

            currentAttack.execute(shooter, ProjectileType.PLAYER_BULLET, angle, customizer);
            player.startCooldownFuoco();
        }
    }

    /**
     * Aggiorna il pattern d'attacco corrente e i tempi di ricarica in base al livello attuale del giocatore.
     */
    private void updateAttackPatternByLevel() {
        int level = player.getLevel();
        EntityRecord data = player.getEntityRecord();

        if (data == null || data.player() == null) {
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