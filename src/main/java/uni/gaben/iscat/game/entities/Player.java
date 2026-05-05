package uni.gaben.iscat.game.entities;

import uni.gaben.iscat.game.GamePanel;
import uni.gaben.iscat.game.KeyHandler;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyHandler;

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;

        setDefaultValues();
        getPlayerSprites();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
    }

    public void getPlayerSprites() {
        String path = "/uni/gaben/iscat/sprites/battle_ship_1.png";
        entitySprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
    }

    public void update() {
        if (keyHandler.upPressed) {
            y -= speed;
        }
        if (keyHandler.downPressed) {
            y += speed;
        }
        if (keyHandler.rightPressed) {
            x += speed;
        }
        if (keyHandler.leftPressed) {
            x -= speed;
        }
    }

    public void draw(GraphicsContext gc) {
        if (entitySprite != null) {
            gc.drawImage(entitySprite, x, y, gp.tileSize, gp.tileSize);
        }
    }
}