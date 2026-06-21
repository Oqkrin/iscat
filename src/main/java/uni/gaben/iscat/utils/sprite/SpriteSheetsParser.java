package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.InputStream;
import java.util.Objects;

public class SpriteSheetsParser {

    private final Image sheet;
    public final int frameWidth;
    public final int frameHeight;

    private final int maxColumnsCount;
    private final int rowsCount;
    private final int[] framesPerRow;
    private final Image[][] slicedFrames;

    /** Construct from classpath path (internal JAR). */
    public SpriteSheetsParser(String path, int frameWidth, int frameHeight) {
        this(Objects.requireNonNull(
                        SpriteSheetsParser.class.getResourceAsStream(path),
                        "Sprite not found: " + path),
                frameWidth,
                frameHeight);
    }

    /** Construct from an arbitrary InputStream (e.g. external file). */
    public SpriteSheetsParser(InputStream imageStream, int frameWidth, int frameHeight) {
        this.sheet = new Image(imageStream);
        this.frameWidth  = frameWidth;
        this.frameHeight = frameHeight;

        this.maxColumnsCount = Math.max(1, (int) sheet.getWidth()  / frameWidth);
        this.rowsCount       = Math.max(1, (int) sheet.getHeight() / frameHeight);

        PixelReader px = sheet.getPixelReader();

        this.framesPerRow = new int[rowsCount];
        this.slicedFrames = new Image[rowsCount][];

        for (int r = 0; r < rowsCount; r++) {
            int lastNonTransparent = 0;
            for (int c = maxColumnsCount - 1; c >= 0; c--) {
                if (!isRegionFullyTransparent(px, c * frameWidth, r * frameHeight, frameWidth, frameHeight)) {
                    lastNonTransparent = c;
                    break;
                }
            }
            int count = lastNonTransparent + 1;
            framesPerRow[r] = count;

            slicedFrames[r] = new Image[count];
            for (int c = 0; c < count; c++) {
                slicedFrames[r][c] = new WritableImage(
                        px,
                        c * frameWidth,
                        r * frameHeight,
                        frameWidth,
                        frameHeight
                );
            }
        }
    }

    // ── Public API (unchanged) ──────────────────────────────────────────────

    public Image getSheet() { return sheet; }
    public int getTotalFrames() { return maxColumnsCount; }
    public int getTotalStates() { return rowsCount; }

    public int getFrameCount(int state) {
        if (state < 0 || state >= rowsCount) return 1;
        return framesPerRow[state];
    }

    public int[] getFramesPerRow() { return framesPerRow.clone(); }

    public Image getFrame(int state, int frame) {
        if (state < 0 || state >= rowsCount) return null;
        if (frame < 0 || frame >= framesPerRow[state]) return null;
        return slicedFrames[state][frame];
    }

    private static boolean isRegionFullyTransparent(PixelReader px, int x, int y, int w, int h) {
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                if ((px.getArgb(x + col, y + row) >>> 24) != 0) return false;
            }
        }
        return true;
    }
}