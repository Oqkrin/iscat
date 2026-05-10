package uni.gaben.iscat.game.components.entities.npcs;

import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.game.components.entities.LivingEntityModel;

/**
 * Nemico generico.
 * Estende {@link LivingEntityModel} (fisica + salute) e implementa {@link Collidable}.
 * L'IA verrà aggiunta sovrascrivendo {@link #update} quando il sistema AI sarà pronto.
 */
public class NpcModel extends LivingEntityModel implements Collidable {

    public static final double RADIUS = 16.0;

    public NpcModel(double x, double y) {
        this.x     = x;
        this.y     = y;
        this.hp    = 50;
        this.maxHp = 50;
        this.mass  = 8.0;
        this.name  = "Enemy";
        this.spriteSize = RADIUS * 2; // Sprite size based on collision radius
    }

    @Override public double getCollisionRadius() { return RADIUS; }

    @Override
    public void onCollision(Collidable other) {
        //override
    }


}
