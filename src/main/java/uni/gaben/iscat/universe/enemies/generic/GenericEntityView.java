package uni.gaben.iscat.universe.enemies.generic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Controller grafico (Vista) unificato e polimorfico per il rendering dei nemici generici.
 * Sostituisce le classi View specifiche instanziando dinamicamente lo spritesheet e i dati geometrici
 * dei frame estratti dal database SQLite tramite l'oggetto {@link GenericEntitySettings}.
 * Sfrutta un ciclo di animazione a riga singola a frame-rate costante per la riproduzione degli stati,
 * delegando la traslazione dei sistemi di coordinate polari e la rotazione alla superclasse {@link AbstractEntityView}.
 */
public class GenericEntityView extends AbstractEntityView<GenericEntityModel>
        implements Drawable<GenericEntityModel>, DrawableSpriteSheet {

    /** Durata standard di esposizione di un singolo frame dell'animazione (espressa in secondi, pari a 6 FPS). */
    private static final double FRAME_DURATION = 1.0 / 6.0;

    /** Compensazione angolare in gradi necessaria per allineare l'orientamento nativo delle texture (Nord) all'asse di Dyn4J. */
    private static final double ANGULAR_OFFSET_DEG = 270.0;

    private final SpriteSheetsParser sheet;
    private final SpriteSheetsAnimator animator;

    /**
     * Costruisce la componente visiva del nemico, configurando la scala di rendering
     * ed estraendo le texture raster dal magazzino centralizzato {@link SpritesLibrary}.
     *
     * @param settings DTO contenente i percorsi dei file grafici e le dimensioni atomiche dei frame.
     */
    public GenericEntityView(GenericEntitySettings settings) {
        this.spriteScale = settings.scale;

        // Recupero dello spritesheet indicizzato per evitare letture ridondanti su disco
        this.sheet = SpritesLibrary.getInstance().getSprite(
                settings.spritePath,
                settings.frameW,
                settings.frameH);

        int totalFrames = (sheet != null) ? sheet.getTotalFrames() : 1;
        int totalStates = (sheet != null) ? sheet.getTotalStates() : 1;

        this.animator = new SpriteSheetsAnimator(FRAME_DURATION, totalFrames, totalStates);
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() { return sheet; }

    @Override
    public SpriteSheetsAnimator getAnimator()  { return animator; }

    /**
     * Aggiorna i puntatori temporali dell'animatore interno avanzandone il ciclo di vita.
     * Invocato ad ogni tick logico dal loop di rendering della UI.
     *
     * @param dt Delta time (tempo trascorso dall'ultimo frame).
     */
    @Override
    public void updateAnimator(double dt) {
        animator.update(dt);
    }

    /**
     * Punto di ingresso principale della pipeline di disegno dell'entità sul Canvas.
     * Configura il contesto grafico applicando le matrici di trasformazione rototraslazionali.
     *
     * @param entity Il modello logico da cui attingere le coordinate spaziali d'origine.
     * @param gc     Il contesto grafico 2D di destinazione del Canvas JavaFX.
     */
    @Override
    public void draw(GenericEntityModel entity, GraphicsContext gc) {
        if (entity == null) return;
        setupGraphicsContextAndDrawContent(entity, gc, ANGULAR_OFFSET_DEG, true);
    }

    /**
     * Esegue il disegno effettivo dei pixel sul Canvas una volta che il sistema di riferimento
     * è stato centrato e orientato correttamente nello spazio.
     * Calcola l'indice del frame corrente e applica le sfumature cromatiche d'ambiente (Global Tint).
     *
     * @param entity Il modello logico del nemico.
     * @param gc     Il contesto grafico 2D su cui effettuare il blit della texture.
     * @param x      Coordinata X di destinazione locale nell'area d'origine del frame.
     * @param y      Coordinata Y di destinazione locale nell'area d'origine del frame.
     * @param width  Larghezza scalata finale del rettangolo di destinazione.
     * @param height Altezza scalata finale del rettangolo di destinazione.
     */
    @Override
    protected void drawContent(GenericEntityModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        if (sheet == null) return;

        int currentRow  = animator.getCurrentState(); // Costante 0: profilo ad animazione singola lineare
        int maxFrames   = sheet.getTotalFrames();
        int localFrame  = (int) (animator.getTime() / FRAME_DURATION) % Math.max(maxFrames, 1);

        Image frame = sheet.getFrame(currentRow, localFrame);
        if (frame == null) return;

        // Applicazione dinamica del filtro colore dipendente dal tema di gioco attivo (es. Palette Notturna/Inversa)
        Image tinted = ThemeManager.getInstance().getTintedImage(
                frame,
                ThemeManager.getInstance().globalTintProperty().get());

        gc.drawImage(tinted, x, y, width, height);
    }
}