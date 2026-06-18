package uni.gaben.iscat.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
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

    public AnimatedCanvas() {
        super(0, 0);
    }

    public AnimatedCanvas(double size) {
        super(size, size);
    }

    /**
     * Sets the duration of each frame in seconds.
     */
    public void setFrameDuration(double duration) {
        this.currentFrameDuration = duration;
        if (animator != null) {
            // Recalculate durations for all frames/states
            animator.constantDurationFiller(
                    duration,
                    spriteSheet.getTotalFrames(),
                    spriteSheet.getTotalStates()
            );
        }
    }

    /**
     * Loads a sprite sheet with default frame size (32x32).
     */
    public void loadSkin(String path) {
        loadSkin(path, 32, 32);
    }

    /**
     * Loads a sprite sheet with custom frame dimensions.
     */
    public void loadSkin(String path, int frameW, int frameH) {
        stop(); // stop any running animation

        // Use the central sprite library – same as EntityRenderer
        spriteSheet = SpritesLibrary.getInstance().getSprite(path, frameW, frameH);
        if (spriteSheet == null || spriteSheet.getSheet() == null) {
            System.err.println("[AnimatedCanvas] Failed to load sprite: " + path);
            return;
        }

        // Create animator with the same logic as EntityRenderer
        int states = spriteSheet.getTotalStates();
        int framesPerState = spriteSheet.getFramesPerRow().length; // assuming uniform rows
        animator = new SpriteSheetsAnimator(currentFrameDuration, framesPerState, states);
        animator.constantDurationFiller(
                currentFrameDuration,
                spriteSheet.getTotalFrames(),
                states
        );

        // Reset cache
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

    /**
     * Resizes the canvas to a square of the given size.
     */
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

        int state = Math.clamp(animator.getCurrentState(), 0, spriteSheet.getTotalStates() - 1);
        int frame = Math.clamp(animator.getCurrentFrame(), 0, spriteSheet.getTotalFrames() - 1);

        // Use the same tint as EntityRenderer (accent secondary for UI)
        Color tint = ThemeManager.getInstance().getAccentSecondary();

        // Only regenerate the tinted frame if something changed
        if (cachedTintedFrame == null
                || !tint.equals(lastAppliedTint)
                || state != lastRow
                || frame != lastCol) {

            Image rawFrame = spriteSheet.getFrame(state, frame);
            if (rawFrame != null) {
                // Apply tint using the same utility as EntityRenderer
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

    /**
     * Forces the cache to be rebuilt on next render.
     */
    public void invalidateCache() {
        this.cachedTintedFrame = null;
        this.lastAppliedTint = null;
        this.lastRow = -1;
        this.lastCol = -1;
    }

    /**
     * Stops the animation timer.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastTime = 0;
    }
}