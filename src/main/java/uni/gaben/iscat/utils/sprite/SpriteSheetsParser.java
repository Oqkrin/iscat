package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Objects;

public class SpriteSheetsParser {
    private final Image sheet;
    public final int frameWidth;
    public final int frameHeight;
    private final int columnsCount;
    private final int rowsCount;

    // Cache bidimensionale permanente dei singoli frame microscopici
    private final Image[][] slicedFrames;

    public SpriteSheetsParser(String path, int frameWidth, int frameHeight) {
        this.sheet = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(path)));
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        this.columnsCount = Math.max(1, (int) sheet.getWidth() / frameWidth);
        this.rowsCount = Math.max(1, (int) sheet.getHeight() / frameHeight);

        // Generiamo i singoli sottomoduli d'immagine all'avvio
        this.slicedFrames = new Image[rowsCount][columnsCount];
        for (int r = 0; r < rowsCount; r++) {
            for (int c = 0; c < columnsCount; c++) {
                this.slicedFrames[r][c] = new WritableImage(
                        sheet.getPixelReader(),
                        c * frameWidth,
                        r * frameHeight,
                        frameWidth,
                        frameHeight
                );
            }
        }
    }

    public Image getSheet() {
        return sheet;
    }

    public int getTotalFrames() { return columnsCount; }
    public int getTotalStates() { return rowsCount; }

    /**
     * Restituisce istantaneamente il riferimento al singolo frame pre-tagliato.
     * Operazione O(1) con zero overhead di memoria.
     */
    public Image getFrame(int state, int frame) {
        if (state < 0 || state >= rowsCount || frame < 0 || frame >= columnsCount) {
            return null;
        }
        return slicedFrames[state][frame];
    }
}