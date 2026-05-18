package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadSettings.*;

public class IscatWormHeadView extends AbstractEntityView<IscatWormHeadModel>
        implements Drawable<IscatWormHeadModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/iscat_worm_head.png";
    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormHeadView() {
        spriteScale = IscatWormHeadSettings.SCALE;
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(SPRITE_PATH, DIM_SPRITE, DIM_SPRITE);
        this.animator = new SpriteSheetsAnimator(0.035, spriteSheet.getTotalFrames(), spriteSheet.getTotalStates());
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(IscatWormHeadModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);

        // Esegue la pipeline centralizzata applicando la correzione a 180° dell'asset
        setupGraphicsContextAndDrawContent(entity, gc, 180.0);
    }

    @Override
    protected void drawContent(IscatWormHeadModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // Il canvas è pre-configurato: disegna lo sprite alle coordinate locali precalcolate
        drawSprite(gc, x, y, width, height);
    }
}