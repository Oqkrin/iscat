package uni.gaben.iscat.iscat_game.universe.iscats.worm;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

public class IscatWormView extends AbstractEntityView<IscatWormSegment>
        implements Drawable<IscatWormSegment>, DrawableSpriteSheet {

    // Una view per tipo: cache statica per non ricaricare gli stessi sprite
    private static final java.util.Map<IscatWormSegment.Type, IscatWormView> CACHE
            = new java.util.EnumMap<>(IscatWormSegment.Type.class);

    public static IscatWormView forType(IscatWormSegment.Type type) {
        return CACHE.computeIfAbsent(type, IscatWormView::new);
    }

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    private IscatWormView(IscatWormSegment.Type type) {
        spriteScale = switch (type) {
            case HEAD -> IscatWormSettings.HEAD_SCALE;
            case BODY -> IscatWormSettings.BODY_SCALE;
            case TAIL -> IscatWormSettings.TAIL_SCALE;
        };

        spriteSheet = SpritesLibrary.getInstance().getSprite(
                getSpritePath(type),
                (int) IscatWormSettings.DIM_SPRITE,
                (int) IscatWormSettings.DIM_SPRITE);

        animator = new SpriteSheetsAnimator(
                0.08,
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1);
    }

    @Override
    public void draw(IscatWormSegment entity, GraphicsContext gc) {
        if (entity == null || entity.isConsumed()) return;
        animator.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 180.0);
    }

    @Override
    protected void drawContent(IscatWormSegment entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
        if (entity.getType() == IscatWormSegment.Type.HEAD) {
            drawHpBar(entity, gc);
        }
    }

    private String getSpritePath(IscatWormSegment.Type type) {
        return switch (type) {
            case HEAD -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_head.png";
            case BODY -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_body_part.png";
            case TAIL -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_tail.png";
        };
    }

    @Override public SpriteSheetsParser getSpriteSheet()   { return spriteSheet; }
    @Override public SpriteSheetsAnimator getAnimator()    { return animator; }
}