package uni.gaben.iscat.view.skin;

import javafx.scene.control.Button;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.view.components.AnimatedCanvas;

/**
 * Pulsante personalizzato che visualizza l'anteprima animata di una skin di gioco.
 * Gestisce il ridimensionamento dinamico del canvas interno in base alle dimensioni del pulsante stesso.
 */
public class SkinButton extends Button {

    // Stesso rapporto utilizzato nel controller
    public static final double SKIN_TO_BUTTON_RATIO = 0.9;

    private final AnimatedCanvas canvas;

    /**
     * Costruisce un pulsante SkinButton inizializzando il canvas animato con i dati della skin passata.
     *
     * @param skin   Il record dell'entità (contiene il percorso dello sprite e la dimensione del frame)
     * @param width  Larghezza iniziale del pulsante
     * @param height Altezza iniziale del pulsante
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

        // Associa la dimensione del canvas ai cambiamenti di dimensione del pulsante
        widthProperty().addListener((obs, old, newVal) -> updateCanvasSize());
        heightProperty().addListener((obs, old, newVal) -> updateCanvasSize());

        updateCanvasSize(); // Regolazione iniziale
    }

    /** Aggiorna proporzionalmente le dimensioni geometriche del canvas di anteprima applicando il fattore di scala. */
    private void updateCanvasSize() {
        double size = Math.min(getWidth(), getHeight()) * SKIN_TO_BUTTON_RATIO;
        if (size > 0) {
            canvas.resize(size);
        }
    }
}