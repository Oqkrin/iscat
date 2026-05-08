package uni.gaben.iscat.game.components.entities.player.projectile;

import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.game.components.entities.LivingEntityModel;
import uni.gaben.iscat.game.components.entities.PhysicalEntityModel;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;

public class ProjectileModel extends PhysicalEntityModel implements Collidable {
    private int ticksLeft = 120; // Vive per 2 secondi a 60fps
    public static final double RADIUS = 4.0;
    private boolean colpito = false;

    public ProjectileModel(Vec2 pos, Vec2 vel) {
        this.x = pos.x;
        this.y = pos.y;
        this.velocity = vel;
        this.mass = 0.1;
        this.maxSpeed = 30.0;
        this.drag = 1.0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        ticksLeft--;
    }

    public boolean isExpired() {
        return ticksLeft <= 0;
    }

    @Override
    public double getCollisionRadius() {
        return 3.0;
    }

    @Override
    public Vec2 getColliderCenter() {
        return getPosition();
    }

    @Override
    public void onCollision(Collidable other) {
        // niente collision con player e proiettile
        if (other instanceof PlayerModel) return;
        if (other instanceof ProjectileModel) return;

        if (!colpito) {
            if (other instanceof LivingEntityModel target) {
                target.takeDamage(10); // Se colpisci un Bomber, lui subirà danno
                System.out.println("HO COLPITO QUALCOSA: " + other.getClass().getSimpleName());
            }
            colpito = true;
            // Facciamo sparire il proiettile al prossimo cleanup
            this.ticksLeft = 0;
        }
    }
}
