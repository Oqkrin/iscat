package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.physics.Vec2;

public class Projectile extends PhysicalEntity implements Collidable {
    private int ticksLeft = 120; // Vive per 2 secondi a 60fps
    public static final double RADIUS = 4.0;
    private boolean colpito = false;

    public Projectile(Vec2 pos, Vec2 vel) {
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
        if (other instanceof Player) return;
        if (other instanceof Projectile) return;

        if (!colpito) {
            System.out.println("HO COLPITO QUALCOSA: " + other.getClass().getSimpleName());
            colpito = true;

            // Facciamo sparire il proiettile al prossimo cleanup
            this.ticksLeft = 0;
        }
    }
}