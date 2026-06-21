package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import java.util.ArrayDeque;

/**
 * Pool statica di proiettili ottimizzata per il thread singolo di JavaFX.
 * Riduce a zero le allocazioni di memoria riutilizzando le istanze dei proiettili dismessi.
 */
public final class ProjectilePool {

    private static final ArrayDeque<ProjectileModel> pool         = new ArrayDeque<>(256);
    private static final ArrayDeque<StunThreadProjectileModel> stunPool = new ArrayDeque<>(64);

    private static final int MAX_POOL_SIZE      = 5000;
    private static final int MAX_STUN_POOL_SIZE = 500;

    private ProjectilePool() {}

    /**
     * Recupera un proiettile pronto dall'apposita pool in base al tipo richiesto.
     */
    public static ProjectileModel acquire(ProjectileType type) {
        if (type == ProjectileType.STUN_BULLET) {
            StunThreadProjectileModel p = stunPool.pollLast();
            if (p == null) {
                p = new StunThreadProjectileModel();
            } else {
                p.reset(type);
            }
            return p;
        }

        ProjectileModel p = pool.pollLast();
        if (p == null) {
            p = new ProjectileModel(type);
        } else {
            p.reset(type);
        }
        return p;
    }

    /**
     * Rilascia un proiettile generico nella pool.
     * Sfrutta l'overloading per evitare controlli di tipo (instanceof) a runtime.
     */
    public static void release(ProjectileModel p) {
        // Se a runtime viene passato l'esatto sottotipo Stun, la JVM userà l'overload specifico.
        // Questo controllo fa da salvagente se viene passato come tipo base.
        if (p instanceof StunThreadProjectileModel stun) {
            release(stun);
            return;
        }

        if (!p.isInPool() && pool.size() < MAX_POOL_SIZE) {
            p.setInPool(true);
            pool.addLast(p);
        }
    }

    /**
     * Rilascia un proiettile di tipo Stun nella rispettiva pool dedicata.
     * Risolto a tempo di compilazione tramite Overloading per le massime prestazioni.
     */
    public static void release(StunThreadProjectileModel p) {
        if (!p.isInPool() && stunPool.size() < MAX_STUN_POOL_SIZE) {
            p.setInPool(true);
            stunPool.addLast(p);
        }
    }
}