package uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_tail;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_tail.IscatWormTailSettings.*;

/**
 * Vista standardizzata per la coda dell'Iscat Worm.
 * Sfrutta la pipeline centralizzata per gestire in automatico le matrici di trasformazione relative.
 */
public class IscatWormTailView extends AbstractEntityView<IscatWormTailModel>
        implements Drawable<IscatWormTailModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_worm_tail.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormTailView() {
        spriteScale = IscatWormTailSettings.SCALE;
        // 1. Recupero dello spritesheet dalla libreria condivisa
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                DIM_SPRITE,
                DIM_SPRITE
        );

        // 2. Configurazione dell'animatore (~5.5 FPS)
        this.animator = new SpriteSheetsAnimator(
                0.18,
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
    public void draw(IscatWormTailModel entity, GraphicsContext gc) {
        // 3. Avanzamento temporale dell'animazione
        animator.update(UU.UNIVERSE_TICK);

        // 4. Esegue la pipeline centralizzata applicando la correzione a 180° dell'asset
        setupGraphicsContextAndDrawContent(entity, gc, 180.0);
    }

    @Override
    protected void drawContent(IscatWormTailModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // 5. Il canvas è pre-configurato al centro esatto: disegna lo sprite alle coordinate locali filtrate
        drawSprite(gc, x, y, width, height);
    }
}