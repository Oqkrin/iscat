package uni.gaben.iscat.game.components.entities.npcs.iscat_mother;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.settings.VisualSettings;
import java.util.Objects;

/**
 * Renderer per IscatMother con supporto a:
 * - Sprite sheet a 2 frame
 * - Rotazione verso la direzione
 * - Scala personalizzabile (più grande)
 */
public class IscatMotherView implements EntityRenderer<IscatMotherModel> {

    private static final double TILE_SIZE = VisualSettings.DIMENSIONE_TILE;
    private static final double NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;

    private final Image spriteSheet;

    // ====================== CONFIGURAZIONE ======================
    private static final double SCALE = 5;           // ← Cambia qui per regolare la grandezza
    private static final double DRAW_SIZE = TILE_SIZE * SCALE;

    private static final int FRAME_WIDTH = 128;
    private static final int FRAME_HEIGHT = 128;
    private static final int TOTAL_FRAMES = 2;
    private static final double FRAME_DURATION = 0.45;   // secondi per frame (0.3 = più veloce, 0.6 = più lenta)

    public IscatMotherView() {
        this.spriteSheet = new Image(Objects.requireNonNull(
                IscatMotherView.class.getResourceAsStream("/uni/gaben/iscat/sprites/iscat_mother.png")));
    }

    @Override
    public void draw(GraphicsContext gc, IscatMotherModel mother) {
        double cx = mother.getX() + TILE_SIZE / 2.0;
        double cy = mother.getY() + TILE_SIZE / 2.0;

        // Calcolo del frame corrente per l'animazione
        int currentFrame = (int) ((System.nanoTime() / 1_000_000_000.0) / FRAME_DURATION) % TOTAL_FRAMES;
        int sourceX = currentFrame * FRAME_WIDTH;

        gc.save();

        // Posizionamento e rotazione
        gc.translate(cx, cy);
        gc.rotate(-(mother.getDirectionAngle() + NORTH_OFFSET));

        // Disegno dello sprite con scala aumentata
        gc.drawImage(
                spriteSheet,
                sourceX, 0,                    // Posizione sorgente nello sprite sheet
                FRAME_WIDTH, FRAME_HEIGHT,     // Dimensione sorgente
                -DRAW_SIZE / 2.0,              // Posizione destinazione (centrata)
                -DRAW_SIZE / 2.0,              // Posizione destinazione (centrata)
                DRAW_SIZE,                     // Larghezza destinazione
                DRAW_SIZE                      // Altezza destinazione
        );

        gc.restore();
    }
}