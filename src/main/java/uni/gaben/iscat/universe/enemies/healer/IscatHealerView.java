package uni.gaben.iscat.universe.enemies.healer;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

public class IscatHealerView extends AbstractEntityView<IscatHealerModel>
        implements Drawable<IscatHealerModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;
    private final Cooldown healingAnimation = new Cooldown();
    private final double healRadiusM;

    public IscatHealerView(IscatHealerModel entity) {
        var s = entity.getSettings();
        spriteScale = s.scale;
        healRadiusM = s.customParam1 > 0 ? s.customParam1 : IscatHealerSettings.HEAL_RADIUS_M;

        this.spriteSheet = SpritesLibrary.getInstance()
                .getSprite(s.spritePath, s.frameW, s.frameH);

        this.animator = new SpriteSheetsAnimator(
                UU.UNIVERSE_TICK * 4,
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1);
    }

    @Override public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }
    @Override public SpriteSheetsAnimator getAnimator()  { return animator; }

    @Override
    public void draw(IscatHealerModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        healingAnimation.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 90.0, false);
    }

    @Override
    protected void drawContent(IscatHealerModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        if (healingAnimation.isReady()) {
            entity.shockwave().trigger(
                    UU.UNIVERSE_TICK * 45,
                    UU.mToPx(healRadiusM),
                    UU.mToPx(healRadiusM) / 10);
            healingAnimation.start(1);
        }

        drawSprite(gc, x, y, width, height);
        drawShockwave(gc, 0, 0, entity.shockwave());
    }

    @Override
    public void setAnimatorTime(double time) {
        animator.setTime(time);
    }
}