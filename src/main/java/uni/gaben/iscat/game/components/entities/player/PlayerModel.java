package uni.gaben.iscat.game.components.entities.player;

import uni.gaben.iscat.game.components.entities.LivingEntityModel;
import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.interfaces.Drawable;
import uni.gaben.iscat.game.utils.interfaces.HasRenderer;
import uni.gaben.iscat.game.utils.interfaces.Spawnable;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.utils.Cooldown;

/**
 * PlayerModel — pure state.
 *
 * Owns: physics config, health, cooldown timers, dash phase state.
 * Does NOT own: input flags, callbacks, audio, projectile spawning,
 *               or any "when to act" decision logic → see player/controller/.
 *
 * Controllers interact via:
 *   - isScattoDisponibile() / isInScatto()  — query state
 *   - executeScatto(directionAngle)          — apply dash impulse + start timers
 *   - isSparo Disponibile()                  — query state
 *   - getSpawnData(directionAngle)           — returns projectile spawn data (no side effects)
 *   - startCooldownFuoco()                   — start fire cooldown after shot
 */
public class PlayerModel extends LivingEntityModel implements Collidable, HasRenderer, Spawnable {

    private final PlayerView VIEW = new PlayerView();

    private final Cooldown cooldownScatto = new Cooldown();
    private final Cooldown cooldownFuoco  = new Cooldown();
    private final Cooldown faseScatto     = new Cooldown();

    public PlayerModel(double startX, double startY) {
        this.x         = startX;
        this.y         = startY;
        this.hp        = PlayerSettings.HP_INIZIALE;
        this.maxHp     = PlayerSettings.HP_MASSIMO;
        this.mass      = PlayerSettings.MASSA;
        this.name      = "Player";
        this.spriteSize = PlayerSettings.DIMENSIONE_SPRITE;
        this.drag      = PlayerSettings.ATTRITO;
        this.maxSpeed  = PlayerSettings.VELOCITA_MAX;
        this.deadZone  = PlayerSettings.ZONA_MORTA;
    }

    // -------------------------------------------------------------------------
    // Physics — drag/maxSpeed vary during dash phase
    // -------------------------------------------------------------------------

    @Override
    public void integrate(double dt) {
        if (faseScatto.isActive()) {
            this.drag     = PlayerSettings.ATTRITO_SCATTO;
            this.maxSpeed = PlayerSettings.VELOCITA_MAX_SCATTO;
        } else {
            this.drag     = PlayerSettings.ATTRITO;
            this.maxSpeed = PlayerSettings.VELOCITA_MAX;
        }
        super.integrate(dt);
        faseScatto.tick();
        cooldownScatto.tick();
        cooldownFuoco.tick();
    }

    // -------------------------------------------------------------------------
    // Dash state — queried and driven by PlayerDodgeController
    // -------------------------------------------------------------------------

    /** {@code true} if dash is off cooldown and can be executed. */
    public boolean isScattoDisponibile() { return cooldownScatto.isReady(); }

    /** {@code true} while the active dash phase (reduced drag) is running. */
    public boolean isInScatto() { return faseScatto.isActive(); }

    /**
     * Applies the dash impulse and starts the dash phase + cooldown timers.
     * Called by PlayerDodgeController after verifying the cooldown is ready.
     * Pure state mutation — no callbacks, no audio.
     */
    public void executeScatto(double directionAngle) {
        double rad = Math.toRadians(directionAngle);
        velocity = velocity.add(
                Math.cos(rad) * PlayerSettings.IMPULSO_SCATTO,
                Math.sin(rad) * PlayerSettings.IMPULSO_SCATTO);
        faseScatto.set(PlayerSettings.DURATA_SCATTO_TICK);
        cooldownScatto.set(PlayerSettings.COOLDOWN_SCATTO_TICK);
    }

    // -------------------------------------------------------------------------
    // Shooting state — queried and driven by PlayerShootingController
    // -------------------------------------------------------------------------

    /** {@code true} if the fire cooldown has expired and a shot can be fired. */
    public boolean isSparoDisponibile() { return cooldownFuoco.isReady(); }

    /**
     * Returns the projectile spawn position and velocity for the current direction.
     * Pure calculation — no side effects, no callbacks.
     * Called by PlayerShootingController to create the projectile.
     */
    public Vec2[] getSpawnData(double directionAngle) {
        double rad = Math.toRadians(directionAngle);
        Vec2 spawnPos = new Vec2(x + spriteSize / 2.0, y + spriteSize / 2.0);
        Vec2 bulletVel = new Vec2(
                Math.cos(rad) * PlayerSettings.VELOCITA_PROIETTILE,
                Math.sin(rad) * PlayerSettings.VELOCITA_PROIETTILE);
        return new Vec2[]{ spawnPos, bulletVel };
    }

    /** Starts the fire cooldown. Called by PlayerShootingController after spawning the projectile. */
    public void startCooldownFuoco() {
        cooldownFuoco.set(PlayerSettings.COOLDOWN_FUOCO_TICK);
    }

    // -------------------------------------------------------------------------
    // Collidable
    // -------------------------------------------------------------------------

    @Override public double getCollisionRadius() { return PlayerSettings.RAGGIO_COLLISIONE; }
    @Override public Vec2   getColliderCenter()  { return super.getColliderCenter(); }
    @Override public void   onCollision(Collidable other) {}
    @Override public int    getCollisionLayer()  { return LAYER_PLAYER; }
    @Override public int    getCollisionMask()   { return LAYER_ENEMY | LAYER_OBJECT; }

    // -------------------------------------------------------------------------
    // HasRenderer
    // -------------------------------------------------------------------------

    @Override public Drawable<PlayerModel> getRenderer() { return VIEW; }

    // -------------------------------------------------------------------------
    // Alive
    // -------------------------------------------------------------------------

    @Override public void die() { /* TODO: death animation */ }
}
