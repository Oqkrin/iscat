package uni.gaben.iscat.game.model.entities;

/**
 * Base minimale per ogni oggetto nel mondo di gioco.
 * Solo identità e posizione grezza.
 *
 * Fisica → {@link PhysicalEntity}.
 * Salute → {@link LivingEntity}.
 * Collisioni → interfaccia {@link uni.gaben.iscat.game.model.interfaces.Collidable}.
 */
public abstract class Entity {

    protected double x;
    protected double y;
    protected String name = "";

    public double getX()    { return x; }
    public double getY()    { return y; }
    public String getName() { return name; }

    public void setX(double x)       { this.x = x; }
    public void setY(double y)       { this.y = y; }
    public void setName(String name) { this.name = name; }
}
