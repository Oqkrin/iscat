package uni.gaben.iscat.game.model.physics;

import uni.gaben.iscat.game.model.GameSettings;
import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.interfaces.Physical;

/**
 * Utility per risolvere collisioni fisicamente accurate.
 * Usa conservazione del momento e coefficiente di restituzione.
 */
public final class CollisionPhysics {
    
    private CollisionPhysics() {}
    
    /**
     * Risolve una collisione elastica tra due corpi fisici collidibili.
     * Usa impulso basato su conservazione del momento.
     * 
     * @param a primo corpo
     * @param b secondo corpo
     * @param restitution coefficiente di restituzione (0-1)
     */
    public static void resolveElasticCollision(Physical a, Physical b, double restitution) {
        // Ottieni i centri di collisione (non le posizioni top-left!)
        Vec2 centerA, centerB;
        
        if (a instanceof Collidable ca) {
            centerA = ca.getColliderCenter();
        } else {
            centerA = a.getPosition();
        }
        
        if (b instanceof Collidable cb) {
            centerB = cb.getColliderCenter();
        } else {
            centerB = b.getPosition();
        }
        
        // Vettore di collisione (da A a B)
        double dx = centerB.x - centerA.x;
        double dy = centerB.y - centerA.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.001) {
            // Corpi sovrapposti completamente - separa in direzione casuale
            dx = 1.0;
            dy = 0.0;
            distance = 1.0;
        }
        
        // Normalizza il vettore di collisione
        double nx = dx / distance;
        double ny = dy / distance;
        
        // Velocità correnti
        Vec2 velA = a.getVelocity();
        Vec2 velB = b.getVelocity();
        
        // Velocità relativa (A rispetto a B)
        double dvx = velA.x - velB.x;
        double dvy = velA.y - velB.y;
        
        // Velocità relativa lungo la normale di collisione
        // Se negativo: si stanno avvicinando
        // Se positivo: si stanno allontanando
        double dvn = dvx * nx + dvy * ny;
        
        // Se si stanno già allontanando, non fare nulla
        if (dvn >= 0) {
            return;
        }
        
        // Masse
        double m1 = a.getMass();
        double m2 = b.getMass();
        
        // Calcola impulso scalare: J = -(1 + e) * dvn / (1/m1 + 1/m2)
        double impulseScalar = -(1.0 + restitution) * dvn / ((1.0 / m1) + (1.0 / m2));
        
        // Vettore impulso lungo la normale
        double jx = impulseScalar * nx;
        double jy = impulseScalar * ny;
        
        // Applica impulso (J/m per ogni corpo)
        a.setVelocity(new Vec2(
            velA.x + jx / m1,
            velA.y + jy / m1
        ));
        
        b.setVelocity(new Vec2(
            velB.x - jx / m2,
            velB.y - jy / m2
        ));
        
        // Separa i corpi per evitare overlap persistente
        separateBodies(a, b, nx, ny);
    }
    
    /**
     * Separa due corpi che si stanno sovrapponendo.
     * Sposta entrambi proporzionalmente alla loro massa inversa.
     */
    private static void separateBodies(Physical a, Physical b, double nx, double ny) {
        double m1 = a.getMass();
        double m2 = b.getMass();
        double totalMass = m1 + m2;
        
        // Separazione totale necessaria
        double separation = 3.0;
        
        // Sposta proporzionalmente alla massa inversa (più leggero si muove di più)
        double moveA = separation * (m2 / totalMass);
        double moveB = separation * (m1 / totalMass);
        
        Vec2 posA = a.getPosition();
        Vec2 posB = b.getPosition();
        
        a.setPosition(new Vec2(posA.x - nx * moveA, posA.y - ny * moveA));
        b.setPosition(new Vec2(posB.x + nx * moveB, posB.y + ny * moveB));
    }
    
    /**
     * Risolve una collisione usando il coefficiente di restituzione di default.
     */
    public static void resolveElasticCollision(Physical a, Physical b) {
        resolveElasticCollision(a, b,
                GameSettings.COLLISION_RESTITUTION);
    }
}
