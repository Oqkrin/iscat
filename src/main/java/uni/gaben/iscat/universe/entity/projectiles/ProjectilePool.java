package uni.gaben.iscat.universe.entity.projectiles;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectilePool {
    private static final ConcurrentLinkedQueue<ProjectileProjectileModel> pool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger poolSize = new AtomicInteger(0);

    public static ProjectileProjectileModel acquire(ProjectileType type) {
        ProjectileProjectileModel p = pool.poll();
        if (p == null) {
            p = new ProjectileProjectileModel(type);
        } else {
            poolSize.decrementAndGet();
            p.setInPool(false);
            p.setOnCollision(null);
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

    public static void release(ProjectileProjectileModel p) {
        if (!p.isInPool() && poolSize.get() < 5000) {
            p.setInPool(true);
            pool.offer(p);
            poolSize.incrementAndGet();
        }
    }
}
