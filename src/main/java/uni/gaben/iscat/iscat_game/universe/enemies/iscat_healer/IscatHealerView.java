package uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer.IscatHealerSettings.*;

public class IscatHealerView extends AbstractEntityView<IscatHealerModel>
        implements Drawable<IscatHealerModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_healer.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;
    private final Cooldown healingAnimation = new Cooldown();

    public IscatHealerView() {
        spriteScale = ISCATHEALER.scale;

        this.spriteSheet = SpritesLibrary.getInstance()
                .getSprite(SPRITE_PATH, (int) ISCATHEALER.dimSprite, (int) ISCATHEALER.dimSprite);

        this.animator = new SpriteSheetsAnimator(
                UU.UNIVERSE_TICK * 4,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(IscatHealerModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        healingAnimation.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 90.0);
    }

    @Override
    protected void drawContent(IscatHealerModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {


        if(healingAnimation.isReady()) {
            entity.shockwave().trigger(UU.UNIVERSE_TICK*45, UU.mToPx(HEAL_RADIUS_M), UU.mToPx(HEAL_RADIUS_M)/10);
            healingAnimation.start(1);
        }

        drawSprite(gc, x, y, width, height);
        drawShockwave(gc, 0, 0, entity.shockwave());
        drawHpBar(entity, gc);
    }
}
