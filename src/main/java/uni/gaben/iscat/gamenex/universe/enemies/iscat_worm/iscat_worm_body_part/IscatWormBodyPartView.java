package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part.IscatWormBodyPartSettings.*;

/**
 * Vista standardizzata per i segmenti del corpo dell'Iscat Worm.
 * Implementa {@link Drawable} sfruttando la pipeline centrale dell'engine.
 */
public class IscatWormBodyPartView extends AbstractEntityView<IscatWormBodyPartModel>
        implements Drawable<IscatWormBodyPartModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_worm_body_part.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormBodyPartView() {
        spriteScale = IscatWormBodyPartSettings.SCALE;
        // 1. Caricamento centralizzato dello spritesheet tramite la libreria condivisa
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                (int) DIM_SPRITE,
                (int) DIM_SPRITE
        );

        // 2. Configurazione dell'animatore (~10 FPS)
        this.animator = new SpriteSheetsAnimator(
                0.10,
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1
        );
    }

    // --- Implementazione dei getter richiesti da DrawableSpriteSheet ---

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(IscatWormBodyPartModel entity, GraphicsContext gc) {
        // 3. Update temporale dell'animazione agganciato ai tick del loop di gioco
        animator.update(UU.UNIVERSE_TICK);

        // 4. Esegue la pipeline centralizzata applicando la correzione a 180° dell'asset orientato
        setupGraphicsContextAndDrawContent(entity, gc, 180.0);
    }

    @Override
    protected void drawContent(IscatWormBodyPartModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // 5. Rendering del segmento: l'origine (0,0) del canvas si trova già centrata e orientata
        drawSprite(gc, x, y, width, height);
    }
}