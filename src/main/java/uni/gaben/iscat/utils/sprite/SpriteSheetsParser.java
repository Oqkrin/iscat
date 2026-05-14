package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;

import java.util.Objects;

public class SpriteSheetsParser {
    private final Image sheet;
    public final int frameWidth;
    public final int frameHeight;
    private final int columnsCount;
    private final int rowsCount;

    public SpriteSheetsParser(String path, int frameWidth, int frameHeight) {
        this.sheet = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(path)));
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        // Usiamo Math.max per prevenire divisioni per zero se l'immagine ha problemi
        this.columnsCount = Math.max(1, (int) sheet.getWidth() / frameWidth);
        this.rowsCount = Math.max(1, (int) sheet.getHeight() / frameHeight);
    }

    public Image getSheet() {
        return sheet;
    }

    public int getTotalFrames() { return columnsCount; }
    public int getTotalStates() { return rowsCount; }

}