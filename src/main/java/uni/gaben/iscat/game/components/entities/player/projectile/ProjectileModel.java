package uni.gaben.iscat.game.components.entities.player.projectile;

import uni.gaben.iscat.game.components.entities.LivingEntityModel;
import uni.gaben.iscat.game.components.entities.PhysicalEntityModel;
import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.interfaces.Drawable;
import uni.gaben.iscat.game.utils.interfaces.Expirable;
import uni.gaben.iscat.game.utils.interfaces.HasRenderer;
import uni.gaben.iscat.game.utils.interfaces.Spawnable;
import uni.gaben.iscat.game.utils.physics.Vec2;

/**
 * Proiettile sparato dal giocatore.
 *
 * Implementa:
 * - Expirable  → rimosso automaticamente da GameModel quando scade
 * - HasRenderer → si auto-registra con il proprio renderer
 * - Spawnable  → lifecycle hooks
 * - Collidable → collision layer PROJECTILE, colpisce solo ENEMY
 */
public class ProjectileModel extends PhysicalEntityModel
        implements Collidable, HasRenderer, Expirable, Spawnable {

    public static final double RADIUS = 4.0;
    private static final int   LIFETIME_TICKS = 120; // 2 sec @ 60fps
    private static final int   DAMAGE = 10;

    private static final ProjectileView VIEW = new ProjectileView();

    private int     ticksLeft = LIFETIME_TICKS;
    private boolean colpito   = false;

    public ProjectileModel(Vec2 pos, Vec2 vel) {
        this.x        = pos.x;
        this.y        = pos.y;
        this.velocity = vel;
        this.mass     = 0.1;
        this.maxSpeed = 30.0;
        this.drag     = 1.0;
    }

    // -------------------------------------------------------------------------
    // Updatable
    // -------------------------------------------------------------------------

    @Override
    public void update(double dt) {
        super.update(dt);
        ticksLeft--;
    }

    // -------------------------------------------------------------------------
    // Expirable
    // -------------------------------------------------------------------------

    @Override
    public boolean isExpired() { return ticksLeft <= 0; }

    // -------------------------------------------------------------------------
    // Collidable
    // -------------------------------------------------------------------------

    @Override public double getCollisionRadius() { return RADIUS; }
    @Override public Vec2   getColliderCenter()  { return getPosition(); }

    @Override public int getCollisionLayer() { return LAYER_PROJECTILE; }
    /** Projectiles only collide with enemies — layer mask eliminates player/projectile checks. */
    @Override public int getCollisionMask()  { return LAYER_ENEMY; }

    @Override
    public void onCollision(Collidable other) {
        if (colpito) return;
        // Layer mask already ensures we only reach here for ENEMY targets,
        // but we still check LivingEntityModel to apply damage safely.
        if (other instanceof LivingEntityModel target) {
            target.takeDamage(DAMAGE);
        }
        colpito   = true;
        ticksLeft = 0; // expire immediately
    }

    // -------------------------------------------------------------------------
    // HasRenderer
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public Drawable<ProjectileModel> getRenderer() { return VIEW; }
}
