package uni.gaben.iscat.universe.consumables.heart;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary; // Adjusted to match your library package path

import static uni.gaben.iscat.universe.consumables.heart.HeartSettings.DIM_SPRITE;

/**
 * Vista standardizzata per l'entità Hearth.
 * Utilizza la SpritesLibrary e l'interfaccia DrawableSpriteSheet per eliminare
 * la gestione manuale del pixel reading e della cache dei frame.
 */
public class HeartView extends AbstractEntityView<HeartModel>
        implements Drawable<HeartModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/boosts/heart.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public HeartView() {
        // 1. Carica lo spritesheet delegando il parsing e il caching alla libreria globale
        spriteScale = HeartSettings.SCALE;
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                (int) DIM_SPRITE,
                (int) DIM_SPRITE
        );

        // 2. Configura l'animatore standard con la durata dei frame desiderata (es: 0.1s)
        this.animator = new SpriteSheetsAnimator(
                0.1,
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1
        );
    }

    // --- Implementazione dei getter di DrawableSpriteSheet ---

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(HeartModel entity, GraphicsContext gc) {
        // 3. Fai avanzare il timer dell'animazione secondo i tick di gioco
        animator.update(UU.UNIVERSE_TICK);

        // 4. Invia l'entità nella pipeline centrale di rendering
        setupGraphicsContextAndDrawContent(entity, gc, 270.0);
    }

    @Override
    protected void drawContent(HeartModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // 5. drawSprite calcola in automatico riga, colonna, tinting globale e disegna nel centro local space
        drawSprite(gc, x, y, width, height);
    }
}