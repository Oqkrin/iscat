package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectilePool {
    private static final ConcurrentLinkedQueue<ProjectileModel> pool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger poolSize = new AtomicInteger(0);

    public static ProjectileModel acquire(ProjectileType type) {
        ProjectileModel p = pool.poll();
        if (p == null) {
            p = new ProjectileModel(type);
        } else {
            poolSize.decrementAndGet();
            p.setInPool(false);
            p.clearOnCollisions();
            p.setKilledByProjectile(false);
            p.setShouldRemove(false);
            p.setEnabled(true);
            p.setAtRest(false);
            p.getTransform().setTranslation(0, 0);
            p.getTransform().setRotation(0);
            p.setLinearVelocity(0, 0);
            p.setAngularVelocity(0);
            p.clearAccumulatedForce();
            p.clearAccumulatedTorque();
            p.setType(type);
        }
        return p;
    }

    public static void release(ProjectileModel p) {
        if (!p.isInPool() && poolSize.get() < 5000) {
            p.setInPool(true);
            pool.offer(p);
            poolSize.incrementAndGet();
        }
    }
}
