package uni.gaben.iscat.gamenex.universe.enemies.iscat_eater;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.enemies.iscat_eater.IscatEaterSettings.*;

/**
 * Vista standardizzata per l'entità Eater.
 * Sfrutta la pipeline della classe base per astrarre la gestione delle matrici di trasformazione relative.
 */
public class IscatEaterView extends AbstractEntityView<IscatEaterModel>
        implements Drawable<IscatEaterModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser;
    private final SpriteSheetsAnimator animator;

    private static final String SPRITE_SHEET_PATH = "/uni/gaben/iscat/sprites/enemies/eater.png";

    public IscatEaterView() {
        spriteScale = IscatEaterSettings.SCALE;
        // 1. Inizializzazione dello spritesheet tramite la libreria globale
        this.spriteSheetsParser = SpritesLibrary.getInstance().getSprite(SPRITE_SHEET_PATH, DIM_SPRITE, DIM_SPRITE);

        // 2. Inizializzazione dell'animatore con frame rate standard (~24 FPS)
        this.animator = new SpriteSheetsAnimator(
                1.0 / 24.0,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalFrames() : 1,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalStates() : 1
        );
    }

    // --- Implementazione dei getter richiesti da DrawableSpriteSheet ---

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheetsParser; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(IscatEaterModel entity, GraphicsContext gc) {
        // 3. Update temporale basato sul delta temporale dell'universo di gioco
        animator.update(UU.UNIVERSE_TICK);

        // [Opzionale] Gestione dinamica degli stati dell'animazione
        // if (entity.isEating()) animator.setState(1); else animator.setState(0);

        // 4. Determina l'angolo finale: se ROTATION_TOWARDS_PLAYER è disattivato,
        // calcoliamo l'offset inverso all'angolo base in modo da forzare il disegno dritto (0.0°).
        setAngle(entity);
        double structuralOffset = ROTATION_TOWARDS_PLAYER ? 0.0 : -rotDeg;

        // 5. Invia l'entità alla pipeline standard dell'engine
        setupGraphicsContextAndDrawContent(entity, gc, structuralOffset);
    }

    @Override
    protected void drawContent(IscatEaterModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // 6. Il canvas si trova già centrato e ruotato correttamente.
        // Forniamo x e y (-w/2, -h/2) per centrare perfettamente lo sprite sull'ancora fisica.
        drawSprite(gc, x, y, width, height);
    }
}