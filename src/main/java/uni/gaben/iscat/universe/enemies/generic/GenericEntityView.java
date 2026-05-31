package uni.gaben.iscat.universe.enemies.generic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * A single View class that replaces all per-enemy View classes.
 * The spritesheet path and frame dimensions come directly from
 * GenericEntitySettings (loaded from the Entita DB table).
 * Animation is a simple single-row loop — row 0, all frames at 1/6s each.
 * This is intentionally minimal: more complex animation state machines
 * (like IscatMasterView) still live in their own dedicated View classes.
 * The render pipeline reuses AbstractEntityView.setupGraphicsContextAndDrawContent
 * exactly as every other view in the codebase does.
 */
public class GenericEntityView extends AbstractEntityView<GenericEntityModel>
        implements Drawable<GenericEntityModel>, DrawableSpriteSheet {

    private static final double FRAME_DURATION = 1.0 / 6.0;
    private static final double ANGULAR_OFFSET_DEG = 270.0;

    private final SpriteSheetsParser sheet;
    private final SpriteSheetsAnimator animator;

    public GenericEntityView(GenericEntitySettings settings) {
        this.spriteScale = settings.scale;

        this.sheet = SpritesLibrary.getInstance().getSprite(
                settings.spritePath,
                settings.frameW,
                settings.frameH);

        int totalFrames = (sheet != null) ? sheet.getTotalFrames() : 1;
        int totalStates = (sheet != null) ? sheet.getTotalStates() : 1;

        this.animator = new SpriteSheetsAnimator(FRAME_DURATION, totalFrames, totalStates);
    }

    // ── DrawableSpriteSheet ───────────────────────────────────────────────────

    @Override public SpriteSheetsParser getSpriteSheet() { return sheet; }
    @Override public SpriteSheetsAnimator getAnimator()  { return animator; }

    // ── Animator update (called by the render loop each tick) ─────────────────

    @Override
    public void updateAnimator(double dt) {
        animator.update(dt);
    }

    // ── Main draw entry point ─────────────────────────────────────────────────

    @Override
    public void draw(GenericEntityModel entity, GraphicsContext gc) {
        if (entity == null) return;
        setupGraphicsContextAndDrawContent(entity, gc, ANGULAR_OFFSET_DEG, true);
    }

    // ── Actual pixel drawing (called by the pipeline in AbstractEntityView) ───

    @Override
    protected void drawContent(GenericEntityModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        if (sheet == null) return;

        int currentRow  = animator.getCurrentState();  // always 0 for generic enemies
        int maxFrames   = sheet.getTotalFrames();
        int localFrame  = (int) (animator.getTime() / FRAME_DURATION) % Math.max(maxFrames, 1);

        Image frame = sheet.getFrame(currentRow, localFrame);
        if (frame == null) return;

        Image tinted = ThemeManager.getInstance().getTintedImage(
                frame,
                ThemeManager.getInstance().globalTintProperty().get());

        gc.drawImage(tinted, x, y, width, height);

    }
}