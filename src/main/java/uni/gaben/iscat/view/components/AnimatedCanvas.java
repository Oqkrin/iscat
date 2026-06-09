package uni.gaben.iscat.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

/**
 * Canvas ottimizzato per il rendering e l'animazione di sprite-sheet pixel-art
 * con supporto al color-tinting dinamico tramite ThemeManager.
 */
public class AnimatedCanvas extends Canvas {

    private SpriteSheetsParser spriteSheet;
    private SpriteSheetsAnimator animator;
    private AnimationTimer timer;

    private long lastTime = 0;
    private double currentFrameDuration = 0.1;

    private Image cachedTintedFrame;
    private Color lastAppliedTint;
    private int lastRow = -1;
    private int lastCol = -1;

    public AnimatedCanvas() {
        super(0, 0);
    }

    public AnimatedCanvas(double size) {
        super(size, size);
    }

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

    public void loadSkin(String path) {
        loadSkin(path, 32, 32);
    }

    public void loadSkin(String path, int frameW, int frameH) {
        stop();

        spriteSheet = SpritesLibrary.getInstance().getSprite(path, frameW, frameH);
        if (spriteSheet == null || spriteSheet.getSheet() == null) {
            System.err.println("[AnimatedCanvas] Errore: Impossibile caricare lo sprite da: " + path);
            return;
        }

        animator = new SpriteSheetsAnimator(currentFrameDuration, 1, 1);
        animator.constantDurationFiller(
                currentFrameDuration,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );

        invalidateCache();
        startTimer();
    }

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

    public void resize(double size) {
        setWidth(size);
        setHeight(size);
    }

    private void render() {
        if (spriteSheet == null || animator == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setImageSmoothing(false);
        gc.setEffect(new Bloom());

        int sheetRow = Math.clamp(animator.getCurrentState(), 0, spriteSheet.getTotalStates() - 1);
        int sheetColumn = Math.clamp(animator.getCurrentFrame(), 0, spriteSheet.getTotalFrames() - 1);

        Color currentTint = ThemeManager.getInstance().getAccentSecondary();

        // GESTIONE CACHE: Ricalcola la tinta solo se il colore cambia O se l'animatore è passato al frame successivo
        if (cachedTintedFrame == null
                || !currentTint.equals(lastAppliedTint)
                || sheetRow != lastRow
                || sheetColumn != lastCol) {

            // Preleva il frame già tagliato dal parser (leggerissimo)
            Image tinyFrame = spriteSheet.getFrame(sheetRow, sheetColumn);

            if (tinyFrame != null) {
                // Tinge solo il singolo quadratino
                cachedTintedFrame = ThemeManager.getInstance().getTintedImage(tinyFrame, currentTint);
            }

            lastAppliedTint = currentTint;
            lastRow = sheetRow;
            lastCol = sheetColumn;
        }

        // Disegna direttamente il frame tinto senza bisogno di calcolare le coordinate sorgente (sx, sy)
        if (cachedTintedFrame != null) {
            gc.drawImage(
                    cachedTintedFrame,
                    0, 0, getWidth(), getHeight()
            );
        }
    }

    public void invalidateCache() {
        this.cachedTintedFrame = null;
        this.lastAppliedTint = null;
        this.lastRow = -1;
        this.lastCol = -1;
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastTime = 0;
    }
}