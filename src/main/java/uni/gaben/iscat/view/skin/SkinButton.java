package uni.gaben.iscat.view.skin;

import javafx.scene.control.Button;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.view.components.AnimatedCanvas;

public class SkinButton extends Button {

    // Same ratio that was in the controller
    public static final double SKIN_TO_BUTTON_RATIO = 0.9;

    private final AnimatedCanvas canvas;

    /**
     * @param skin   the entity record (holds sprite path and frame size)
     * @param width  initial width of the button
     * @param height initial height of the button
     */
    public SkinButton(EntityRecord skin, double width, double height) {
        getStyleClass().add("skin-button");
        setFocusTraversable(false);
        setUserData(skin.entityKey());

        double baseSize = Math.min(width, height);
        canvas = new AnimatedCanvas(baseSize);
        canvas.loadSkin(skin.spritePath(), skin.frameW(), skin.frameH());
        setGraphic(canvas);

        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);

        // Bind canvas size to button size changes
        widthProperty().addListener((obs, old, newVal) -> updateCanvasSize());
        heightProperty().addListener((obs, old, newVal) -> updateCanvasSize());

        updateCanvasSize(); // initial adjustment
    }

    private void updateCanvasSize() {
        double size = Math.min(getWidth(), getHeight()) * SKIN_TO_BUTTON_RATIO;
        if (size > 0) {
            canvas.resize(size);
        }
    }
}