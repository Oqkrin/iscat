package uni.gaben.iscat.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;

import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.sprite.SpriteUtils;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Canvas ottimizzato per il rendering e l'animazione di sprite-sheet pixel-art
 * con supporto al color-tinting dinamico tramite ThemeManager.
 *
 * <p>Reworked to share sprite loading and animation logic with the entity
 * rendering system, avoiding duplication.</p>
 */
public class AnimatedCanvas extends Canvas {

    private SpriteSheetsParser spriteSheet;
    private SpriteSheetsAnimator animator;
    private AnimationTimer timer;

    private long lastTime = 0;
    private double currentFrameDuration = 0.1;

    // Cached tinted frame (same caching strategy as EntityRenderer)
    private Image cachedTintedFrame;
    private Color lastAppliedTint;
    private int lastRow = -1;
    private int lastCol = -1;

    /** Canvas vuoto con dimensioni iniziali a zero. */
    public AnimatedCanvas() {
        super(0, 0);
    }

    /** Canvas quadrato con dimensione personalizzata. */
    public AnimatedCanvas(double size) {
        super(size, size);
    }

    /** Imposta la durata di ogni frame in secondi. */
    public void setFrameDuration(double duration) {
        this.currentFrameDuration = duration;
        if (animator != null) {
            animator.constantDurationFiller(
                    duration,
                    spriteSheet.getTotalFrames(),
                    spriteSheet.getTotalStates()
            );
        }
    }

    /** Carica uno sprite dal percorso specificato tramite SpritesLibrary. */
    public void loadSkin(String path, int frameW, int frameH) {
        stop();

        spriteSheet = SpritesLibrary.getInstance().getSprite(path, frameW, frameH);
        if (spriteSheet == null || spriteSheet.getSheet() == null) {
            System.err.println("[AnimatedCanvas] Failed to load sprite: " + path);
            return;
        }

        initAnimator();
    }

    /** Carica uno sprite direttamente da un InputStream. */
    public void loadSkin(InputStream imageStream, int frameW, int frameH) {
        stop();

        spriteSheet = new SpriteSheetsParser(imageStream, frameW, frameH);
        if (spriteSheet == null || spriteSheet.getSheet() == null) {
            System.err.println("[AnimatedCanvas] Failed to load sprite from stream");
            return;
        }

        initAnimator();
    }

    /** Inizializza l'animatore per la gestione del tempo e dei frame. */
    private void initAnimator() {

        int states = spriteSheet.getTotalStates();
        int framesPerState = spriteSheet.getFramesPerRow().length;   // number of states = row count
        animator = new SpriteSheetsAnimator(currentFrameDuration, framesPerState, states);
        animator.constantDurationFiller(
                currentFrameDuration,
                spriteSheet.getTotalFrames(),
                states
        );

        invalidateCache();
        startTimer();
    }

    /** Avvia l'AnimationTimer principale del ciclo di gioco. */
    private void startTimer() {
        lastTime = 0;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (animator != null) {
                    animator.update(delta);
                }
                render();
            }
        };
        timer.start();
    }

    /** Ridimensiona il canvas mantenendo la geometria quadrata. */
    public void resize(double size) {
        setWidth(size);
        setHeight(size);
    }

    /** Esegue il rendering effettivo e la ricolorazione software del frame corrente. */
    private void render() {
        if (spriteSheet == null || animator == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setImageSmoothing(false);

        int state = Math.clamp(animator.getCurrentState(), 0, spriteSheet.getTotalStates() - 1);
        int frame = Math.clamp(animator.getCurrentFrame(), 0, spriteSheet.getTotalFrames() - 1);

        Color tint = ThemeManager.getInstance().getAccentSecondary();

        if (cachedTintedFrame == null
                || !tint.equals(lastAppliedTint)
                || state != lastRow
                || frame != lastCol) {

            Image rawFrame = spriteSheet.getFrame(state, frame);
            if (rawFrame != null) {
                cachedTintedFrame = SpriteUtils.tinted(rawFrame, tint);
            } else {
                cachedTintedFrame = null;
            }

            lastAppliedTint = tint;
            lastRow = state;
            lastCol = frame;
        }

        if (cachedTintedFrame != null) {
            gc.drawImage(cachedTintedFrame, 0, 0, getWidth(), getHeight());
        }
    }

    /** Invalida la cache grafica forzando la ricolorazione al frame successivo. */
    public void invalidateCache() {
        this.cachedTintedFrame = null;
        this.lastAppliedTint = null;
        this.lastRow = -1;
        this.lastCol = -1;
    }

    /** Ferma l'AnimationTimer azzerando i parametri temporali. */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastTime = 0;
    }
}