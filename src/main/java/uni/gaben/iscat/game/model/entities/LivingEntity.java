package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Alive;
import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * {@link PhysicalEntity} con salute e direzione.
 * Le sottoclassi sovrascrivono {@link #die()} per animazioni, loot, respawn, ecc.
 */
public abstract class LivingEntity extends PhysicalEntity implements Alive {

    protected int    hp             = 100;
    protected int    maxHp          = 100;
    protected double directionAngle = 0.0;
    
    /** Dimensione dello sprite per calcolare il centro di collisione. Default: 64.0 */
    protected double spriteSize = 64.0;

    @Override public int  getHp()       { return hp; }
    @Override public int  getMaxHp()    { return maxHp; }
    @Override public void setHp(int hp) { this.hp = Math.max(0, Math.min(maxHp, hp)); }
    @Override public void die()         { /* sovrascrivere */ }

    public double getDirectionAngle()             { return directionAngle; }
    public void   setDirectionAngle(double angle) { this.directionAngle = angle; }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        this.hp    = Math.min(hp, maxHp);
    }
    
    // --- Collision Center ---
    
    /**
     * Restituisce il centro dello sprite per la collisione.
     * Default: posizione + spriteSize/2 su entrambi gli assi.
     * Sovrascrivere se serve un calcolo diverso.
     */
    public Vec2 getColliderCenter() {
        return new Vec2(x + spriteSize / 2.0, y + spriteSize / 2.0);
    }
    
    public void setSpriteSize(double size) { this.spriteSize = size; }
    public double getSpriteSize() { return spriteSize; }
    
    // --- Direction Smoothing ---
    
    /**
     * Aggiorna la direzione in modo smooth verso il target.
     * @param dx differenza x verso il target
     * @param dy differenza y verso il target
     * @param smoothing fattore di interpolazione (0-1). Valori più bassi = rotazione più lenta.
     */
    protected void updateDirectionSmooth(double dx, double dy, double smoothing) {
        double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
        
        // Calcola la differenza angolare
        double angleDiff = targetAngle - directionAngle;
        
        // Normalizza la differenza tra -180 e 180
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;
        
        // Applica interpolazione
        directionAngle += angleDiff * smoothing;
    }

    public boolean isDead() {
        return hp <= 0;
    }
}
