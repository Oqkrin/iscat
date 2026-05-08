package uni.gaben.iscat.game.utils.physics;

import uni.gaben.iscat.game.utils.settings.GameSettings;
import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.interfaces.Physical;

/**
 * Utility per risolvere collisioni fisicamente accurate.
 * Usa conservazione del momento e coefficiente di restituzione.
 *
 * Pipeline per ogni coppia in collisione:
 *   1. Calcola la normale di collisione dai centri dei collisori.
 *   2. Calcola la profondità di penetrazione reale (radii - distanza).
 *   3. Separa i corpi esattamente della profondità di penetrazione,
 *      proporzionalmente alla massa inversa (Positional Correction).
 *   4. Applica l'impulso di velocità solo se i corpi si stanno avvicinando.
 */
public final class CollisionPhysics {

    private CollisionPhysics() {}

    /**
     * Risolve una collisione elastica tra due corpi fisici collidibili.
     *
     * @param a           primo corpo
     * @param b           secondo corpo
     * @param restitution coefficiente di restituzione (0 = anelastica, 1 = elastica perfetta)
     */
    public static void resolveElasticCollision(Physical a, Physical b, double restitution) {

        // --- 1. Centri dei collisori ---
        Vec2 centerA = (a instanceof Collidable ca) ? ca.getColliderCenter() : a.getPosition();
        Vec2 centerB = (b instanceof Collidable cb) ? cb.getColliderCenter() : b.getPosition();

        double dx       = centerB.x - centerA.x;
        double dy       = centerB.y - centerA.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Normale di collisione (da A verso B)
        double nx, ny;
        if (distance < 0.001) {
            // Corpi esattamente sovrapposti — usa asse X come fallback
            nx = 1.0;
            ny = 0.0;
            distance = 0.0;
        } else {
            nx = dx / distance;
            ny = dy / distance;
        }

        // --- 2. Profondità di penetrazione reale ---
        double radA = (a instanceof Collidable ca) ? ca.getCollisionRadius() : 0.0;
        double radB = (b instanceof Collidable cb) ? cb.getCollisionRadius() : 0.0;
        double penetration = (radA + radB) - distance;

        // Se non c'è penetrazione (corpi già separati), non fare nulla
        if (penetration <= 0) return;

        // --- 3. Separazione posizionale proporzionale alla massa inversa ---
        double m1        = a.getMass();
        double m2        = b.getMass();
        double invM1     = 1.0 / m1;
        double invM2     = 1.0 / m2;
        double invMTotal = invM1 + invM2;

        // Piccolo slop (1 px) per evitare jitter su corpi a contatto stretto
        double correction = Math.max(penetration - 1.0, 0.0) / invMTotal;

        Vec2 posA = a.getPosition();
        Vec2 posB = b.getPosition();
        a.setPosition(new Vec2(posA.x - nx * correction * invM1,
                               posA.y - ny * correction * invM1));
        b.setPosition(new Vec2(posB.x + nx * correction * invM2,
                               posB.y + ny * correction * invM2));

        // --- 4. Impulso di velocità ---
        Vec2   velA = a.getVelocity();
        Vec2   velB = b.getVelocity();
        double dvn  = (velA.x - velB.x) * nx + (velA.y - velB.y) * ny;

        // Se i corpi si stanno già allontanando, non applicare impulso
        if (dvn >= 0) return;

        // J = -(1 + e) * dvn / (1/m1 + 1/m2)
        double j  = -(1.0 + restitution) * dvn / invMTotal;
        double jx = j * nx;
        double jy = j * ny;

        a.setVelocity(new Vec2(velA.x + jx * invM1, velA.y + jy * invM1));
        b.setVelocity(new Vec2(velB.x - jx * invM2, velB.y - jy * invM2));
    }

    /**
     * Risolve una collisione usando il coefficiente di restituzione di default
     * ({@link GameSettings#COLLISION_RESTITUTION}).
     */
    public static void resolveElasticCollision(Physical a, Physical b) {
        resolveElasticCollision(a, b, GameSettings.COLLISION_RESTITUTION);
    }
}
