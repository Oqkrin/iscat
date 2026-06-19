package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectilePool {
    private static final ConcurrentLinkedQueue<ProjectileModel> pool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger poolSize = new AtomicInteger(0);

    private static final ConcurrentLinkedQueue<StunThreadProjectileModel> stunPool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger stunPoolSize = new AtomicInteger(0);
    private static final int MAX_STUN_POOL_SIZE = 500;

    public static ProjectileModel acquire(ProjectileType type) {
        if (type == ProjectileType.STUN_BULLET) {
            return acquireStun(type);
        }
        return acquireGeneric(type);
    }

    private static ProjectileModel acquireGeneric(ProjectileType type) {
        ProjectileModel p = pool.poll();
        if (p == null) {
            p = new ProjectileModel(type);
        } else {
            poolSize.decrementAndGet();
            p.reset(type);
        }
        return p;
    }

    private static StunThreadProjectileModel acquireStun(ProjectileType type) {
        StunThreadProjectileModel p = stunPool.poll();
        if (p == null) {
            p = new StunThreadProjectileModel();
        } else {
            stunPoolSize.decrementAndGet();
            p.reset(type);
        }
        return p;
    }

    public static void release(ProjectileModel p) {
        if (p instanceof StunThreadProjectileModel stun) {
            releaseStun(stun);
        } else {
            releaseGeneric(p);
        }
    }

    private static void releaseGeneric(ProjectileModel p) {
        if (!p.isInPool() && poolSize.get() < 5000) {
            p.setInPool(true);
            pool.offer(p);
            poolSize.incrementAndGet();
        }
    }

    private static void releaseStun(StunThreadProjectileModel p) {
        if (!p.isInPool() && stunPoolSize.get() < MAX_STUN_POOL_SIZE) {
            p.setInPool(true);
            stunPool.offer(p);
            stunPoolSize.incrementAndGet();
        }
    }
}