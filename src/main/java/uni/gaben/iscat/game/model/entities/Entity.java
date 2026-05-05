package uni.gaben.iscat.game.model.entities;

import javafx.scene.image.Image;

abstract class Entity {

    public int x,y;
    public int speed;
    public int hp;
    public String name;

    // Sprite dell'entità
    public Image entitySprite;
    // Direzione di default
    public String direction;

    public void die(){
        System.out.println("Entity died");
    }
}
