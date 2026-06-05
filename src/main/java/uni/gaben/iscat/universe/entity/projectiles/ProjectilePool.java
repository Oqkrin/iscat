package uni.gaben.iscat.universe.entity.projectiles;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProjectilePool {
    private static final ConcurrentLinkedQueue<Projectile> pool = new ConcurrentLinkedQueue<>();

    public static Projectile acquire(ProjectileType type) {
        Projectile p = pool.poll();
        if (p == null) {
            p = new Projectile(type);
        } else {
            // Reset basic entity states
            p.setShouldRemove(false);
            p.setEnabled(true);
            p.getTransform().setTranslation(0, 0);
            p.getTransform().setRotation(0);
            p.setLinearVelocity(0, 0);
            p.setAngularVelocity(0);
            p.clearAccumulatedForce();
            p.clearAccumulatedTorque();
            
            // This rebuilds the fixture and resets physics properties
            p.setType(type);
        }
        return p;
    }

    public static void release(Projectile p) {
        // Prevent pool from growing infinitely in case of extreme spam
        if (pool.size() < 1000) {
            pool.offer(p);
        }
    }
}
