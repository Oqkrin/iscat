package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.ThemeManager;
import java.util.Objects;

import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartSettings.DIM_SPRITE;
import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartSettings.NUMERO_FRAMES;

public class IscatWormBodyPartView extends AbstractEntityView implements Drawable<IscatWormBodyPartModel> {

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            IscatWormBodyPartModel.class.getResourceAsStream("/uni/gaben/iscat/sprites/iscat_worm_body_part.png")));

    public static final double DRAW_SIZE = IscatWormBodyPartSettings.DIM_SPRITE * IscatWormBodyPartSettings.SCALE;

    private static Image lastTintedSheet;
    private static final Image[] frameCache = new Image[NUMERO_FRAMES];

    @Override
    public void draw(IscatWormBodyPartModel entity, GraphicsContext gc) {
        Color currentTint = ThemeManager.getInstance().globalTintProperty().get();
        Image tintedSheet = ThemeManager.getInstance().getTintedImage(SPRITE_SHEET, currentTint);

        if (tintedSheet != lastTintedSheet || frameCache[0] == null) {
            lastTintedSheet = tintedSheet;
            for (int i = 0; i < NUMERO_FRAMES; i++) {
                frameCache[i] = new WritableImage(
                        tintedSheet.getPixelReader(),
                        i * (int) DIM_SPRITE,
                        0,
                        (int) DIM_SPRITE,
                        (int) DIM_SPRITE
                );
            }
        }

        setAngle(entity);
        setPos(entity);
        setSize(DRAW_SIZE);

        int frameIdx = (int) ((System.nanoTime() / GamenexModel.NANOSECUNIT) / 0.45) % NUMERO_FRAMES;
        Image drawn = frameCache[frameIdx];

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg + 180);
        gc.drawImage(drawn, -DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
        gc.restore();

        drawHpBar(entity, gc);
    }
}