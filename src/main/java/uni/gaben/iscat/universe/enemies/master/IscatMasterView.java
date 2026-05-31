package uni.gaben.iscat.universe.enemies.master;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

public class IscatMasterView extends AbstractEntityView<IscatMasterModel>
        implements Drawable<IscatMasterModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser masterSheet;
    private final SpriteSheetsAnimator masterAnimator;

    public IscatMasterView(IscatMasterModel entity) {
        var s = entity.getSettings();
        spriteScale = s.scale;

        masterSheet = SpritesLibrary.getInstance().getSprite(
                s.spritePath,
                s.frameW,
                s.frameH);

        masterAnimator = new SpriteSheetsAnimator(
                1.0 / 6.0,
                masterSheet != null ? masterSheet.getTotalFrames() : 1,
                masterSheet != null ? masterSheet.getTotalStates() : 1);
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return masterSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return masterAnimator;
    }

    @Override
    public void setAnimatorTime(double time) {
        masterAnimator.setTime(time);
    }

    @Override
    public void draw(IscatMasterModel entity, GraphicsContext gc) {
        if (entity == null || entity.shouldRemove() || masterSheet == null) return;

        // Map animation state directly to the Spritesheet Row (0 = Entrance, 1 = Idle, etc.)
        masterAnimator.setState(entity.getAnimationState().ordinal());

        // Ensure the animator is perfectly in sync with the model's physics tick
        masterAnimator.setTime(entity.getStateTime());

        // Delegate to the superclass for translation, rotation, and internal drawing
        setupGraphicsContextAndDrawContent(entity, gc, 270.0, false);
    }

    @Override
    protected void drawContent(IscatMasterModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {

        int currentRow = masterAnimator.getCurrentState();
        int maxFrames = entity.getFramesForState(entity.getAnimationState());
        double defaultFrameDuration = 1.0 / 6.0;

        // Calculate visual frame based directly on the Model's state time
        int localFrame = (int) (entity.getStateTime() / defaultFrameDuration) % Math.max(maxFrames, 1);

        Image frame = masterSheet.getFrame(currentRow, localFrame);
        if (frame == null) return;

        Image tinted = ThemeManager.getInstance().getTintedImage(
                frame,
                ThemeManager.getInstance().globalTintProperty().get());
        gc.drawImage(tinted, x, y, width, height);

        // Draw internal model state VFX
        drawShockwave(gc, 0, 0, entity.shockwave());
    }
}