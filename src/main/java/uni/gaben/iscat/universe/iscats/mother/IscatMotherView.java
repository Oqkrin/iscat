package uni.gaben.iscat.universe.iscats.mother;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.universe.iscats.mother.IscatMotherSettings.*;

public class IscatMotherView extends AbstractEntityView<IscatMotherModel>
        implements Drawable<IscatMotherModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_mother.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatMotherView() {
        spriteScale = ISCATMOTHER.scale;

        this.spriteSheet = SpritesLibrary.getInstance()
                .getSprite(SPRITE_PATH, (int) ISCATMOTHER.dimSprite, (int) ISCATMOTHER.dimSprite);

        this.animator = new SpriteSheetsAnimator(
                0.4,  // 2.5 FPS — animazione lenta per boss grande
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1
        );
    }

    // --- DrawableSpriteSheet ---

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }


    @Override
    public void draw(IscatMotherModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 180.0);
    }


    @Override
    protected void drawContent(IscatMotherModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
        drawHpBar(entity, gc);
    }
}
