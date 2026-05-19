package uni.gaben.iscat.game.universe.hearth;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary; // Adjusted to match your library package path

import static uni.gaben.iscat.game.universe.hearth.HearthSettings.DIM_SPRITE;

/**
 * Vista standardizzata per l'entità Hearth.
 * Utilizza la SpritesLibrary e l'interfaccia DrawableSpriteSheet per eliminare
 * la gestione manuale del pixel reading e della cache dei frame.
 */
public class HearthView extends AbstractEntityView<HearthModel>
        implements Drawable<HearthModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/hearth.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public HearthView() {
        // 1. Carica lo spritesheet delegando il parsing e il caching alla libreria globale
        spriteScale = HearthSettings.SCALE;
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
    public void draw(HearthModel entity, GraphicsContext gc) {
        // 3. Fai avanzare il timer dell'animazione secondo i tick di gioco
        animator.update(UU.UNIVERSE_TICK);

        // 4. Invia l'entità nella pipeline centrale di rendering
        setupGraphicsContextAndDrawContent(entity, gc, 270.0);
    }

    @Override
    protected void drawContent(HearthModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // 5. drawSprite calcola in automatico riga, colonna, tinting globale e disegna nel centro local space
        drawSprite(gc, x, y, width, height);
    }
}