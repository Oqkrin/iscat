package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Nemico generico.
 * Estende {@link LivingEntity} (fisica + salute) e implementa {@link Collidable}.
 * L'IA verrà aggiunta sovrascrivendo {@link #update} quando il sistema AI sarà pronto.
 */
public class Enemy extends LivingEntity implements Collidable {

    public static final double RADIUS = 16.0;

    public Enemy(double x, double y) {
        this.x     = x;
        this.y     = y;
        this.hp    = 50;
        this.maxHp = 50;
        this.mass  = 8.0;
        this.name  = "Enemy";
    }

    @Override public double getCollisionRadius() { return RADIUS; }
    @Override public Vec2   getColliderCenter()  { return getPosition(); }

    @Override public void onCollision(Collidable other) { /* TODO: danno, rimbalzo */ }
    @Override public void die()                         { /* TODO: loot, animazione */ }
}
