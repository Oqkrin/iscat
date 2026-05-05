package uni.gaben.iscat.game.model.entities;

import javafx.scene.image.Image;
import uni.gaben.iscat.game.model.entities.Entity;

import java.util.Objects;

// Player.java – un'entità con valori di default e sprite
public class Player extends Entity {
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 4;
        // carica lo sprite
        this.sprite = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
    }
}