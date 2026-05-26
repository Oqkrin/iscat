package uni.gaben.iscat.universe.iscats.dasher;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.universe.iscats.dasher.IscatDasherSettings.*;

public class IscatDasherView extends AbstractEntityView<IscatDasherModel>
        implements Drawable<IscatDasherModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_dasher_S.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatDasherView() {
        spriteScale = ISCATDASHER.scale;

        this.spriteSheet = SpritesLibrary.getInstance()
                .getSprite(SPRITE_PATH, (int) ISCATDASHER.dimSprite, (int) ISCATDASHER.dimSprite);

        this.animator = new SpriteSheetsAnimator(
                UU.UNIVERSE_TICK*2,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(IscatDasherModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, -180.0);
    }

    @Override
    protected void drawContent(IscatDasherModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
        drawHpBar(entity, gc);
    }
}
