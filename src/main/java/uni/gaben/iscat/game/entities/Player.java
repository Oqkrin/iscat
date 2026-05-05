package uni.gaben.iscat.game.entities;

import uni.gaben.iscat.game.GamePanel;
import uni.gaben.iscat.game.KeyHandler;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

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
        direction = "up";
    }

    public void getPlayerSprites() {
        String path = "/uni/gaben/iscat/sprites/battle_ship_1.png";
        up1    = new Image(getClass().getResourceAsStream(path));
        up2    = new Image(getClass().getResourceAsStream(path));
        down1  = new Image(getClass().getResourceAsStream(path));
        down2  = new Image(getClass().getResourceAsStream(path));
        right1 = new Image(getClass().getResourceAsStream(path));
        right2 = new Image(getClass().getResourceAsStream(path));
        left1  = new Image(getClass().getResourceAsStream(path));
        left2  = new Image(getClass().getResourceAsStream(path));
    }

    public void update() {
        if (keyHandler.upPressed) {
            direction = "up";
            y -= speed;
        }
        if (keyHandler.downPressed) {
            direction = "down";
            y += speed;
        }
        if (keyHandler.rightPressed) {
            direction = "right";
            x += speed;
        }
        if (keyHandler.leftPressed) {
            direction = "left";
            x -= speed;
        }
    }

    public void draw(GraphicsContext gc) {
        Image image = switch (direction) {
            case "up"    -> up1;
            case "down"  -> down1;
            case "right" -> right1;
            case "left"  -> left1;
            default      -> null;
        };
        if (image != null) {
            gc.drawImage(image, x, y, gp.tileSize, gp.tileSize);
        }
    }
}