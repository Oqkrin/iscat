package uni.gaben.iscat.game.entities;

import uni.gaben.iscat.game.GamePanel;
import uni.gaben.iscat.game.KeyHandler;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.game.MouseHandler;

import java.util.Objects;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyHandler;
    MouseHandler mouseHandler;

    public Player(GamePanel gp, KeyHandler keyHandler, MouseHandler mouseHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;
        this.mouseHandler = mouseHandler;

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
            double centerX = x + (double) gp.tileSize / 2;
            double centerY = y + (double) gp.tileSize / 2;

            // Vettore dal centro del player al mouse
            double dx = mouseHandler.x - centerX;
            double dy = mouseHandler.y - centerY;

            double angle = Math.toDegrees(Math.atan2(dy, dx));

            gc.save();
            gc.translate(centerX, centerY);
            gc.rotate(angle + 90);
            gc.drawImage(entitySprite,
                    -(double) gp.tileSize / 2,
                    -(double) gp.tileSize / 2,
                    gp.tileSize, gp.tileSize);
            gc.restore();
        }
    }
}