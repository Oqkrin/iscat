package uni.gaben.iscat.game.model.entities;

/** Stella di sfondo: solo posizione, niente fisica né salute. Mossa dal sistema parallasse. */
public class Star extends Entity {
    public Star(double x, double y) { this.x = x; this.y = y; }
}
