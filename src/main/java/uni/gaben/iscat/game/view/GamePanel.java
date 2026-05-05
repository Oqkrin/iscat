package uni.gaben.iscat.game.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.game.controller.KeyHandler;
import uni.gaben.iscat.game.controller.MouseHandler;
import uni.gaben.iscat.game.model.entities.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GamePanel extends StackPane {
    // Impostazioni dello schermo
    final int scale = 3;
    public final int tileSize = 32 * scale;
    int FPS = 60;
    boolean FPS_visible = true;

    KeyHandler keyHandler = new KeyHandler();
    MouseHandler mouseHandler = new MouseHandler();
    Player player = new Player(this, keyHandler, mouseHandler);

    Canvas canvas;
    GameLoop gameLoop;

    public GamePanel() {
        canvas = new Canvas(getWidth(), getHeight());
        widthProperty().addListener(observable -> canvas.setWidth(getWidth()));
        heightProperty().addListener(observable -> canvas.setHeight(getHeight()));
        this.getChildren().add(canvas);

        this.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(Color.BLACK, null, null)
        ));

        // Listener per i tasti
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(keyHandler::keyPressed);
                newScene.setOnKeyReleased(keyHandler::keyReleased);
                newScene.setOnMousePressed(mouseHandler::mousePressed);
                newScene.setOnMouseReleased(mouseHandler::mouseReleased);
                newScene.setOnMouseMoved(mouseHandler::mouseMoved);
            }
        });


        this.setFocusTraversable(true);
        this.requestFocus();
    }

    public void startGameThread() {
        // Istanziamo e avviamo il loop
        gameLoop = new GameLoop();
        gameLoop.start();
    }

    // il core del gioco
    private class GameLoop extends AnimationTimer {
        private double drawInterval = 1000000000.0 / FPS;
        private long lastTime = System.nanoTime();
        private double delta = 0;
        private long timer = 0;
        private int drawCount = 0;

        @Override
        public void handle(long currentTime) {
            // Logica del Delta
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                render();
                delta--;
                drawCount++;
            }

            // Controllo FPS
            if (timer >= 1000000000) {
                if (FPS_visible) {
                    System.out.print("FPS: " + drawCount);
                    if (drawCount >= 60) {
                        System.out.println(" - YUPPI!!! :D");
                    } else {
                        System.out.println(" - LAG...");
                    }
                }
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        player.update();
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        player.draw(gc);
    }
}