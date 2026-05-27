package uni.gaben.iscat.iscat_m_view_c;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    // Cache locale per ottimizzare le performance del rendering ed evitare memory leak
    private Image cachedTintedImage;
    private Color lastAppliedTint;
    private Image lastProcessedSheet;

    public AnimatedCanvas() {
        super(0, 0);
    }

    public AnimatedCanvas(double size) {
        super(size, size);
    }

    public void setFrameDuration(double duration) {
        this.currentFrameDuration = duration;
        if (animator != null) {
            // Aggiorna al volo la durata se l'animatore esiste già
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
        stop(); // Resetta e pulisce lo stato precedente

        spriteSheet = SpritesLibrary.getInstance().getSprite(path, frameW, frameH);
        if (spriteSheet == null || spriteSheet.getSheet() == null) {
            System.err.println("[AnimatedCanvas] Errore: Impossibile caricare lo sprite da: " + path);
            return;
        }

        // Inizializza l'animatore con le specifiche dello sprite caricato
        animator = new SpriteSheetsAnimator(currentFrameDuration, 1, 1);
        animator.constantDurationFiller(
                currentFrameDuration,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );

        // Forza il reset della cache per il nuovo foglio sprite
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
        if (spriteSheet == null || animator == null || spriteSheet.getSheet() == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();

        // Pulizia dell'area di disegno
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Pixel-art nitida (disattiva l'anti-aliasing)
        gc.setImageSmoothing(false);

        // Calcolo sicuro delle coordinate sorgente sul foglio sprite
        int sheetRow = Math.clamp(animator.getCurrentState(), 0, spriteSheet.getTotalStates() - 1);
        int sheetColumn = Math.clamp(animator.getCurrentFrame(), 0, spriteSheet.getTotalFrames() - 1);

        double sx = sheetColumn * spriteSheet.frameWidth;
        double sy = sheetRow * spriteSheet.frameHeight;

        // Recupero della tinta globale attiva
        Color currentTint = ThemeManager.getInstance().getAccentSecondary();

        // GESTIONE CACHE: Chiede l'immagine tinta solo se lo sprite o il colore sono cambiati.
        // Evita di saturare la memoria generando 60 immagini al secondo nel render loop.
        if (cachedTintedImage == null
                || !currentTint.equals(lastAppliedTint)
                || spriteSheet.getSheet() != lastProcessedSheet) {

            cachedTintedImage = ThemeManager.getInstance().getTintedImage(spriteSheet.getSheet(), currentTint);
            lastAppliedTint = currentTint;
            lastProcessedSheet = spriteSheet.getSheet();
        }

        // Disegno finale sul canvas con scaling automatico adattivo
        gc.drawImage(
                cachedTintedImage,
                sx, sy,                                   // Coordinate di origine (sorgente)
                spriteSheet.frameWidth, spriteSheet.frameHeight, // Dimensioni del frame ritagliato
                0, 0,                                     // Destinazione (canvas)
                getWidth(), getHeight()                   // Dimensioni finali scalate
        );
    }

    /**
     * Svuota i riferimenti di cache per forzare un rinfresco totale dell'immagine.
     */
    public void invalidateCache() {
        this.cachedTintedImage = null;
        this.lastAppliedTint = null;
        this.lastProcessedSheet = null;
    }

    /**
     * Ferma l'animazione e rilascia le risorse per prevenire memory leak.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastTime = 0;
    }
}