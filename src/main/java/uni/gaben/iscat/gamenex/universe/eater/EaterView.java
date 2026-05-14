package uni.gaben.iscat.gamenex.universe.eater;

import javafx.animation.Animation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.sprite.AnimationController;
import uni.gaben.iscat.utils.sprite.SpriteDrawer;
import uni.gaben.iscat.utils.sprite.SpriteLibrary;

import java.util.Objects;

import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.DIM_SPRITE;
import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.NUMERO_FRAMES;

public class EaterView extends AbstractEntityView implements Drawable<EaterModel> {

    private SpriteDrawer spriteDrawer;
    private final AnimationController animator = new AnimationController();

    private final String SpriteSheetPah = "/uni/gaben/iscat/sprites/eater.png";
    public EaterView() {
        this.spriteDrawer = SpriteLibrary.getInstance().getSprite(SpriteSheetPah, DIM_SPRITE, DIM_SPRITE);
    }

    public static final double DRAW_SIZE = EaterSettings.DIM_SPRITE * EaterSettings.SCALE;

    @Override
    public void draw(EaterModel entity, GraphicsContext gc) {
        animator.update(GamenexModel.TICKUNIT);
        setAngle(entity);
        setPos(entity);
        setSize(DRAW_SIZE);
        gc.save();
        gc.translate(cx, cy);

        // Disegno dello Sprite tramite il sistema centralizzato (gestisce il tint)
        if (spriteDrawer != null) {
            int frame = animator.getCurrentFrameIdx(spriteDrawer.getTotalFrames(), GamenexModel.TICKUNIT*24); //10 fps
            int row = animator.getCurrentState();
            spriteDrawer.draw(gc, row, frame, 0, 0, w, h); // x,y sono 0 perché siamo traslati
        }
        gc.restore();
        drawHpBar(entity, gc);
    }
}
