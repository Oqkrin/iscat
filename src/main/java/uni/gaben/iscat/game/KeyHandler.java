package uni.gaben.iscat.game;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyHandler {

    public boolean upPressed, leftPressed, downPressed, rightPressed;

    public void keyPressed(KeyEvent e) {
        KeyCode code = e.getCode();

        if (code == KeyCode.W || code == KeyCode.UP)    upPressed = true;
        if (code == KeyCode.A || code == KeyCode.LEFT)  leftPressed = true;
        if (code == KeyCode.S || code == KeyCode.DOWN)  downPressed = true;
        if (code == KeyCode.D || code == KeyCode.RIGHT) rightPressed = true;
    }

    public void keyReleased(KeyEvent e) {
        KeyCode code = e.getCode();

        if (code == KeyCode.W || code == KeyCode.UP)    upPressed = false;
        if (code == KeyCode.A || code == KeyCode.LEFT)  leftPressed = false;
        if (code == KeyCode.S || code == KeyCode.DOWN)  downPressed = false;
        if (code == KeyCode.D || code == KeyCode.RIGHT) rightPressed = false;
    }
}