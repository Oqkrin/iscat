package uni.gaben.iscat.iscat_game.universe.iscats.fallen_star_golem;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.iscat_game.universe.iscats.fallen_star_golem.FallenStarGolemSettings.*;

public class FallenStarGolemView extends AbstractEntityView<FallenStarGolemModel>
        implements Drawable<FallenStarGolemModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser;
    private final SpriteSheetsAnimator animator;

    private static final String SPRITE_SHEET_PATH =
            "/uni/gaben/iscat/sprites/enemies/fallen_star_golem.png";

    public FallenStarGolemView() {

        spriteScale = FALLENSTARGOLEM.scale;

        this.spriteSheetsParser = SpritesLibrary.getInstance()
                .getSprite(
                        SPRITE_SHEET_PATH,
                        (int) FALLENSTARGOLEM.dimSprite,
                        (int) FALLENSTARGOLEM.dimSprite
                );

        this.animator = new SpriteSheetsAnimator(
                1.0 / 24.0,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalFrames() : 1,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalStates() : 1
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheetsParser;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(FallenStarGolemModel entity, GraphicsContext gc) {
        if (entity == null) return;

        animator.update(UU.UNIVERSE_TICK);

        setPos(entity);
        setAngle(entity);

        double structuralOffset = 270.0;

        setupGraphicsContextAndDrawContent(entity, gc, structuralOffset);

        drawHpBar(entity, gc);
    }

    @Override
    protected void drawContent(
            FallenStarGolemModel entity,
            GraphicsContext gc,
            double x,
            double y,
            double width,
            double height
    ) {
        drawSprite(gc, x, y, width, height);
    }
}