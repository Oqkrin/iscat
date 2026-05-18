package uni.gaben.iscat.gamenex.universe.iscat_mother;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

public class IscatMotherView extends AbstractEntityView<IscatMotherModel> implements Drawable<IscatMotherModel>, DrawableSpriteSheet {

    private int spriteSize = 128;
    private SpriteSheetsParser  spriteSheetsParser = SpritesLibrary.getInstance().getSprite(IscatMotherModel.class.getResource("sprites/iscat_mother.png").toExternalForm(), spriteSize, spriteSize);
    private SpriteSheetsAnimator spriteSheetsAnimator = new SpriteSheetsAnimator(UU.UNIVERSE_TICK*2, spriteSheetsParser.getTotalFrames(), spriteSheetsParser.getTotalFrames());

    @Override
    protected void drawContent(IscatMotherModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        spriteSheetsAnimator.update(UU.UNIVERSE_TICK);
        drawSprite(gc, x,  y, width, height);
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheetsParser;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return spriteSheetsAnimator;
    }

    @Override
    public void draw(IscatMotherModel entity, GraphicsContext gc) {
        setupGraphicsContextAndDrawContent(entity, gc, 0.0);
    }
}
