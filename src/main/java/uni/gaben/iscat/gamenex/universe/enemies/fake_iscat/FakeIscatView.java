package uni.gaben.iscat.gamenex.universe.enemies.fake_iscat;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.enemies.fake_iscat.FakeIscatSettings.*;

public class FakeIscatView extends AbstractEntityView<FakeIscatModel>
        implements Drawable<FakeIscatModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser;
    private final SpriteSheetsAnimator animator;

    private static final String SPRITE_SHEET_PATH =
            "/uni/gaben/iscat/sprites/enemies/fake_iscat.png";

    public FakeIscatView() {

        spriteScale = FakeIscatSettings.SCALE;

        this.spriteSheetsParser = SpritesLibrary.getInstance()
                .getSprite(
                        SPRITE_SHEET_PATH,
                        DIM_SPRITE,
                        DIM_SPRITE
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
    public void draw(FakeIscatModel entity, GraphicsContext gc) {
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
            FakeIscatModel entity,
            GraphicsContext gc,
            double x,
            double y,
            double width,
            double height
    ) {
        drawSprite(gc, x, y, width, height);
    }
}