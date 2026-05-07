package uni.gaben.iscat.game.model.entities;

/** 
 * Stella di sfondo: solo posizione e dimensione, niente fisica né salute. 
 * Mossa dal sistema parallasse. 
 */
public class Star extends Entity {
    private final double size;
    
    public Star(double x, double y, double size) { 
        this.x = x; 
        this.y = y; 
        this.size = size;
    }
    
    public double getSize() { 
        return size; 
    }
}
