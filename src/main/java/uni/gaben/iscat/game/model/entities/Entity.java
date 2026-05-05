package uni.gaben.iscat.game.model.entities;

import javafx.scene.image.Image;

// Entity.java – classe base astratta
public abstract class Entity {
    public double x, y;            // ora meglio double per la grafica
    public int speed;
    public int hp;
    public String name;
    public Image sprite;
    public double directionAngle;  // angolo in gradi

    public void die() {

    }
}