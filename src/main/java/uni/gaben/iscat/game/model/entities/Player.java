package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.physics.Vec2;
import uni.gaben.iscat.utils.settings.GameSettings;

/**
 * Nave del giocatore.
 * Estende {@link LivingEntity} (fisica + salute) e implementa {@link Collidable}.
 *
 * La spinta viene applicata dal controller ogni tick via {@link #applyForce}.
 * Il dodge applica un impulso istantaneo nella direzione corrente della nave.
 */
public class Player extends LivingEntity implements Collidable {

    /** Tick rimanenti prima che il dodge sia di nuovo disponibile. */
    private int dodgeCooldown = 0;

    /** true se il dodge è stato richiesto questo tick (consumato dal controller). */
    private boolean dodgeRequested = false;

    // ------------------------------------------------------------------ costruttore

    public Player(double startX, double startY) {
        this.x      = startX;
        this.y      = startY;
        this.hp     = 100;
        this.maxHp  = 100;
        this.mass   = GameSettings.PLAYER_MASS;
        this.name   = "Player";
    }

    // ------------------------------------------------------------------ fisica

    /**
     * Integrazione con drag e cap velocità.
     * Il controller chiama {@link #applyForce} prima di questo ogni tick.
     */
    @Override
    public void integrate(double dt) {
        super.integrate(dt);

        // drag
        velocity = velocity.scale(GameSettings.PLAYER_DRAG);

        // cap velocità
        double speed = velocity.magnitude();
        if (speed > GameSettings.PLAYER_MAX_SPEED) {
            velocity = velocity.scale(GameSettings.PLAYER_MAX_SPEED / speed);
        }

        // dead-zone
        if (Math.abs(velocity.x) < 0.01 && Math.abs(velocity.y) < 0.01) {
            velocity = Vec2.ZERO;
        }

        // decrementa cooldown dodge
        if (dodgeCooldown > 0) dodgeCooldown--;
    }

    // ------------------------------------------------------------------ dodge

    /**
     * Segnala che il giocatore vuole eseguire un dodge questo tick.
     * Il controller chiama questo metodo quando rileva SPACE.
     */
    public void requestDodge() {
        dodgeRequested = true;
    }

    /**
     * Esegue il dodge se richiesto e il cooldown è scaduto.
     * Applica un impulso nella direzione corrente della nave.
     * Chiamato dal controller dopo aver impostato {@link #directionAngle}.
     */
    public void processDodge() {
        if (!dodgeRequested) return;
        dodgeRequested = false;
        if (dodgeCooldown > 0) return;

        // impulso nella direzione in cui punta la nave
        double rad = Math.toRadians(directionAngle);
        double ix  = Math.cos(rad) * GameSettings.DODGE_IMPULSE;
        double iy  = Math.sin(rad) * GameSettings.DODGE_IMPULSE;

        // applica direttamente alla velocità (impulso, non forza)
        velocity = velocity.add(ix, iy);

        dodgeCooldown = GameSettings.DODGE_COOLDOWN_TICKS;
    }

    /** true se il dodge è pronto. */
    public boolean isDodgeReady() { return dodgeCooldown == 0; }

    // ------------------------------------------------------------------ Collidable

    @Override public double getCollisionRadius() { return GameSettings.PLAYER_COLLISION_RADIUS; }
    @Override public Vec2   getColliderCenter()  { return getPosition(); }

    @Override
    public void onCollision(Collidable other) { /* gestito dal sistema collisioni */ }

    // ------------------------------------------------------------------ Alive

    @Override
    public void die() { /* TODO: animazione morte */ }
}
