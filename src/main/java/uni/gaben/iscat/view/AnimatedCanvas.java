package uni.gaben.iscat.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

/**
 * Canvas personalizzato che mostra una sprite animato.
 * Serve per visualizzare skin/animazioni dentro menu o UI.
 */
public class AnimatedCanvas extends Canvas {

    private SpriteSheetsParser spriteSheet;
    private SpriteSheetsAnimator animator;
    private AnimationTimer timer;
    private long lastTime = 0;

    // Variabile per gestire la durata del frame (default 0.1 secondi)
    private double currentFrameDuration = 0.1;

    // Costruttore no-arg richiesto da FXML e SceneBuilder
    public AnimatedCanvas() {
        super(0, 0);
    }

    // crea un canvas quadrato
    public AnimatedCanvas(double size) {
        super(size, size);
    }

    /**
     * Imposta la durata di ogni singolo frame in secondi.
     * Più il valore è alto, più l'animazione risulterà lenta.
     * Va chiamato PRIMA di loadSkin().
     */
    public void setFrameDuration(double duration) {
        this.currentFrameDuration = duration;
    }

    // prepariamo il caricamento della skin passando path, height e width
    public void loadSkin(String path) {
        loadSkin(path, 32, 32);
    }

    // carichiamo la skin
    public void loadSkin(String path, int frameW, int frameH) {
        stop(); // fermiamo animazione precedente
        spriteSheet = SpritesLibrary.getInstance().getSprite(path, frameW, frameH);
        if (spriteSheet == null) return;

        // Usiamo la variabile dinamica currentFrameDuration al posto del valore fisso 0.1
        animator = new SpriteSheetsAnimator(currentFrameDuration, 1, 1);
        animator.constantDurationFiller(currentFrameDuration, spriteSheet.getTotalFrames(), spriteSheet.getTotalStates());

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                animator.update(delta);
                render();
            }
        };
        timer.start();
        render();
    }

    public void resize(double size) {
        setWidth(size);
        setHeight(size);
        render();
    }

    private void render() {
        if (spriteSheet == null || animator == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setImageSmoothing(false);

        var img = spriteSheet.getFrame(animator.getCurrentState(), animator.getCurrentFrame());
        if (img != null) {
            gc.drawImage(img, 0, 0, getWidth(), getHeight());
        }
    }

    public void stop() {
        if (timer != null) { timer.stop(); timer = null; }
        lastTime = 0;
    }
}