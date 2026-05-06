package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Alive;

/**
 * {@link PhysicalEntity} con salute.
 * Le sottoclassi sovrascrivono {@link #die()} per animazioni, loot, respawn, ecc.
 */
public abstract class LivingEntity extends PhysicalEntity implements Alive {

    protected int    hp             = 100;
    protected int    maxHp          = 100;
    protected double directionAngle = 0.0;

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
}
