package uni.gaben.iscat.game;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MouseHandler {
    public double x, y;
    public boolean leftPressed;

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) leftPressed = true;
        System.out.println("MousePressed true");
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) leftPressed = false;
        System.out.println("MouseReleased true");
    }

    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        System.out.println("MouseMoved true");
    }
}
