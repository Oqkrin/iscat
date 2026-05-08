package uni.gaben.iscat.game.components.entities;

import uni.gaben.iscat.game.utils.interfaces.Alive;
import uni.gaben.iscat.game.utils.interfaces.Mortal;
import uni.gaben.iscat.game.utils.interfaces.Rotatable;
import uni.gaben.iscat.game.utils.physics.Vec2;

/**
 * {@link PhysicalEntityModel} con salute e direzione.
 * Le sottoclassi sovrascrivono {@link #die()} per animazioni, loot, respawn, ecc.
 */
public abstract class LivingEntityModel extends PhysicalEntityModel implements Alive, Rotatable, Mortal {

    protected int    hp             = 100;
    protected int    maxHp          = 100;
    protected double directionAngle = 0.0;
    
    /** Dimensione dello sprite per calcolare il centro di collisione. Default: 64.0 */
    protected double spriteSize = 64.0;

    // --- Alive ---

    @Override public int  getHp()       { return hp; }
    @Override public int  getMaxHp()    { return maxHp; }
    @Override public void setHp(int hp) { this.hp = Math.max(0, Math.min(maxHp, hp)); }
    @Override public void die()         { /* sovrascrivere */ }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        this.hp    = Math.min(hp, maxHp);
    }

    public boolean isDead() { return hp <= 0; }

    // --- Rotatable ---

    @Override public double getDirectionAngle()             { return directionAngle; }
    @Override public void   setDirectionAngle(double angle) { this.directionAngle = angle; }

    // --- Collision Center ---

    /**
     * Restituisce il centro dello sprite per la collisione.
     * Default: posizione + spriteSize/2 su entrambi gli assi.
     */
    public Vec2 getColliderCenter() {
        return new Vec2(x + spriteSize / 2.0, y + spriteSize / 2.0);
    }

    public void   setSpriteSize(double size) { this.spriteSize = size; }
    public double getSpriteSize()            { return spriteSize; }

    // --- Damage ---

    private Runnable onHurt;

    public void setOnHurt(Runnable callback) { this.onHurt = callback; }

    @Override
    public void takeDamage(double amount) {
        this.hp -= (int) amount;
        if (onHurt != null && hp >= 0) onHurt.run();
        if (this.hp <= 0) { this.hp = 0; die(); }
    }
}
