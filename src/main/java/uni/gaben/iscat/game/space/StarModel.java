package uni.gaben.iscat.game.space;

import uni.gaben.iscat.game.entities.EntityModel;

/** 
 * Stella di sfondo: solo posizione e dimensione, niente fisica né salute. 
 * Mossa dal sistema parallasse. 
 */
public class StarModel extends EntityModel {
    private final double size;
    
    public StarModel(double x, double y, double size) { 
        this.x = x; 
        this.y = y; 
        this.size = size;
    }
    
    public double getSize() { 
        return size; 
    }
}
