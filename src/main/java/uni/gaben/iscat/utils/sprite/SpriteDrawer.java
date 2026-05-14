package uni.gaben.iscat.utils.sprite;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.utils.ThemeManager;

import java.util.Objects;

public class SpriteDrawer {
    private final Image sheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int columnsCount;
    private final int rowsCount;

    public SpriteDrawer(String path, int frameWidth, int frameHeight) {
        this.sheet = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(path)));
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        // Usiamo Math.max per prevenire divisioni per zero se l'immagine ha problemi
        this.columnsCount = Math.max(1, (int) sheet.getWidth() / frameWidth);
        this.rowsCount = Math.max(1, (int) sheet.getHeight() / frameHeight);
    }

    /**
     * Disegna un frame specifico applicando il colore del ThemeManager.
     */
    public void draw(GraphicsContext gc, int stateIdx, int frameIdx, double x, double y, double w, double h) {
        Color currentTint = ThemeManager.getInstance().globalTintProperty().get();
        Image tintedSheet = ThemeManager.getInstance().getTintedImage(sheet, currentTint);

        int safeFrame = frameIdx % columnsCount;

        // Se la View chiede lo stato 1 (Movimento) ma l'immagine ha solo 1 riga (448x32),
        // costringiamo l'indice a rimanere nel limite (cioè 0) per non uscire fuori dai bordi.
        int safeState = Math.min(stateIdx, rowsCount - 1);

        gc.drawImage(
                tintedSheet,
                (double) safeFrame * frameWidth,  // X sorgente (colonna)
                (double) safeState * frameHeight, // Y sorgente (riga)
                frameWidth, frameHeight,          // Dimensioni sorgente
                x - w / 2, y - h / 2,
                w, h
        );
    }

    public int getTotalFrames() { return columnsCount; }
}