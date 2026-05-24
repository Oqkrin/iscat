package uni.gaben.iscat.iscat_game.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;

public interface DrawableSpriteSheet {

    SpriteSheetsParser getSpriteSheet();
    SpriteSheetsAnimator getAnimator();

    default Color getTint() {
        return ThemeManager.getInstance().globalTintProperty().get();
    }

    default void drawSprite(GraphicsContext gc, double x, double y, double w, double h) {
        SpriteSheetsParser sheet = getSpriteSheet();
        SpriteSheetsAnimator anim = getAnimator();

        if (sheet == null || anim == null || sheet.getSheet() == null) return;

        // Recuperiamo i dati dall'animatore
        int currentState = anim.getCurrentState();
        int currentFrame = anim.getCurrentFrame(); // Allineato con SpriteSheetsAnimator

        Image renderedImage = ThemeManager.getInstance().getTintedImage(sheet.getSheet(), getTint());

        int sheetRow = Math.clamp(currentState, 0, sheet.getTotalStates() - 1);
        int sheetColumn = Math.clamp(currentFrame, 0, sheet.getTotalFrames() - 1);

        double sx = sheetColumn * sheet.frameWidth;
        double sy = sheetRow * sheet.frameHeight;

        gc.drawImage(
                renderedImage,
                sx, sy,
                sheet.frameWidth, sheet.frameHeight,
                x, y, // Posizionamento centrato
                w, h
        );
    }
}