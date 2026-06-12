package uni.gaben.iscat.universe.entity.projectiles;

import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectilePool {
    private static final Map<String, ConcurrentLinkedQueue<GameEntity>> pool = new ConcurrentHashMap<>();
    private static final AtomicInteger poolSize = new AtomicInteger(0);

    public static GameEntity acquire(String key, UniverseModel universe) {
        ConcurrentLinkedQueue<GameEntity> queue = pool.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
        GameEntity p = queue.poll();
        if (p == null) {
            p = EntityFactory.spawn(key, 0, 0, universe, null);
            // Non viene aggiunto subito all'universo perché chi lo richiede lo setterà.
            if (universe != null) universe.removeEntity(p); 
        } else {
            poolSize.decrementAndGet();
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
            p.setEndurance(p.getMaxEndurance());
        }
        return p;
    }

    public static void release(GameEntity p) {
        if (p == null || p.getRecord() == null) return;
        String key = p.getRecord().identity().entityKey();
        ConcurrentLinkedQueue<GameEntity> queue = pool.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
        
        if (poolSize.get() < 5000) {
            queue.offer(p);
            poolSize.incrementAndGet();
        }
    }
}
