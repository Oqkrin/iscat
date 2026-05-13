package uni.gaben.iscat.gamenex.universe.eater;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.ThemeManager;

import java.util.Objects;

import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.DIM_SPRITE;
import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.NUMERO_FRAMES;

public class EaterView extends AbstractEntityView implements Drawable<EaterModel> {

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            EaterModel.class.getResourceAsStream("/uni/gaben/iscat/sprites/eater.png")));

    public static final double DRAW_SIZE = EaterSettings.DIM_SPRITE * EaterSettings.SCALE;

    private static Image lastTintedSheet;
    private static final Image[] frameCache = new Image[NUMERO_FRAMES];

    @Override
    public void draw(EaterModel entity, GraphicsContext gc) {
        Color currentTint = ThemeManager.getInstance().globalTintProperty().get();
        Image tintedSheet = ThemeManager.getInstance().getTintedImage(SPRITE_SHEET, currentTint);

        // Only split the sheet into frames if the tinted sheet has changed
        if (tintedSheet != lastTintedSheet) {
            lastTintedSheet = tintedSheet;
            for (int i = 0; i < NUMERO_FRAMES; i++) {
                frameCache[i] = new WritableImage(
                        tintedSheet.getPixelReader(),
                        i * DIM_SPRITE, 0, DIM_SPRITE, DIM_SPRITE
                );
            }
        }



        int frameIdx = (int) ((System.nanoTime() / GamenexModel.NANOSECUNIT) / 0.4) % NUMERO_FRAMES;

        setAngle(entity);
        setPos(entity);
        setSize(DRAW_SIZE);
        gc.save();
        gc.translate(cx, cy);
        gc.drawImage(frameCache[frameIdx], -DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
        gc.restore();
        drawHpBar(entity, gc);
    }
}
