package uni.gaben.iscat.game.entities;

import uni.gaben.iscat.game.GamePanel;
import uni.gaben.iscat.game.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity{
    GamePanel gp;
    KeyHandler keyHandler;

    public Player(GamePanel gp, KeyHandler keyHandler){
        this.gp = gp;
        this.keyHandler = keyHandler;

        setDefaultValues();
        getPlayerSprites();
    }

    public void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "up";
    }

    public void getPlayerSprites(){
        try {
            up1    = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            up2    = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            down1  = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            down2  = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            right1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            right2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            left1  = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
            left2  = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if(keyHandler.upPressed) {
            direction = "up";
            y -= speed;
        }
        else if (keyHandler.downPressed) {
            direction = "down";
            y += speed;
        }
        else if (keyHandler.rightPressed) {
            direction = "right";
            x += speed;
        }
        else if (keyHandler.leftPressed) {
            direction = "left";
            x -= speed;
        }
    }

    public void draw(Graphics2D g2){
        //g2.setColor(Color.white);
        //g2.fillRect(x,y,50,50);
        BufferedImage image = switch (direction) {
            case "up" -> up1;
            case "down" -> down1;
            case "right" -> right1;
            case "left" -> left1;
            default -> null;
        };
        g2.drawImage(image,x,y,gp.tileSize,gp.tileSize,null);
    }
}
