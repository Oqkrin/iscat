package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Alive;
import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.interfaces.Gravitational;
import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Buco nero: corpo gravitazionale massiccio con orizzonte degli eventi.
 * Attrae tutti i {@link uni.gaben.iscat.game.model.interfaces.Physical} nel raggio.
 * Qualsiasi {@link Alive} che tocca il raggio di collisione viene distrutto.
 * Estende {@link PhysicalEntity} per poter essere a sua volta attratto da altri corpi.
 */
public class BlackHole extends PhysicalEntity implements Gravitational, Collidable {

    private double gravitationalConstant;
    private double gravityRadius;
    private double collisionRadius;

    /**
     * @param x                      posizione X
     * @param y                      posizione Y
     * @param mass                   massa (kg di gioco)
     * @param gravitationalConstant  G locale — più alto = attrazione più forte
     * @param gravityRadius          distanza massima in cui si sente la gravità (px)
     * @param collisionRadius        raggio dell'orizzonte degli eventi (px)
     */
    public BlackHole(double x, double y, double mass,
                     double gravitationalConstant, double gravityRadius,
                     double collisionRadius) {
        this.x                     = x;
        this.y                     = y;
        this.mass                  = mass;
        this.gravitationalConstant = gravitationalConstant;
        this.gravityRadius         = gravityRadius;
        this.collisionRadius       = collisionRadius;
        this.name                  = "BlackHole";
        
        // BlackHole è immobile: nessun attrito, nessuna velocità massima
        this.drag = 1.0;      // No drag (mantiene velocità)
        this.maxSpeed = 0.0;  // Immobile (non può muoversi)
        this.deadZone = 0.0;  // No dead-zone
    }

    @Override public double getGravitationalConstant() { return gravitationalConstant; }
    @Override public double getGravityRadius()         { return gravityRadius; }
    @Override public double getCollisionRadius()       { return collisionRadius; }
    @Override public Vec2   getColliderCenter()        { return getPosition(); }

    /** Distrugge qualsiasi {@link Alive} che entra nell'orizzonte. */
    @Override
    public void onCollision(Collidable other) {
        if (other instanceof Alive a) a.takeDamage(Integer.MAX_VALUE);
    }

    public void setGravitationalConstant(double g) { this.gravitationalConstant = g; }
    public void setGravityRadius(double r)         { this.gravityRadius = r; }
    public void setCollisionRadius(double r)       { this.collisionRadius = r; }
}
