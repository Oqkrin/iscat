package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.Objects;

/**
 * Parses a spritesheet into individual frames, automatically detecting and
 * trimming trailing fully-transparent frames on a per-row basis.
 *
 * <p>This allows a single spritesheet to contain animation rows of varying
 * lengths without wasted blank frames advancing the animation.</p>
 */
public class SpriteSheetsParser {

    private final Image sheet;
    public final int frameWidth;
    public final int frameHeight;

    /** Maximum columns present in the image (the sheet's physical column count). */
    private final int maxColumnsCount;
    private final int rowsCount;

    /**
     * Per-row actual frame counts after trailing-transparency trimming.
     * {@code framesPerRow[r]} is always >= 1.
     */
    private final int[] framesPerRow;

    /** Cache: [row][frame] – only allocated up to framesPerRow[row] columns. */
    private final Image[][] slicedFrames;

    public SpriteSheetsParser(String path, int frameWidth, int frameHeight) {
        this.sheet = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(path)));
        this.frameWidth  = frameWidth;
        this.frameHeight = frameHeight;

        this.maxColumnsCount = Math.max(1, (int) sheet.getWidth()  / frameWidth);
        this.rowsCount       = Math.max(1, (int) sheet.getHeight() / frameHeight);

        PixelReader px = sheet.getPixelReader();

        // ── Per-row frame detection ─────────────────────────────────────────
        this.framesPerRow  = new int[rowsCount];
        this.slicedFrames  = new Image[rowsCount][];

        for (int r = 0; r < rowsCount; r++) {
            // Walk backward from the last column to find the first non-transparent frame
            int lastNonTransparent = 0;
            for (int c = maxColumnsCount - 1; c >= 0; c--) {
                if (!isRegionFullyTransparent(px, c * frameWidth, r * frameHeight, frameWidth, frameHeight)) {
                    lastNonTransparent = c;
                    break;
                }
            }
            int count = lastNonTransparent + 1;   // always >= 1
            framesPerRow[r] = count;

            // Slice only the frames we actually need
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

    // ── Public API ──────────────────────────────────────────────────────────

    public Image getSheet() { return sheet; }

    /** Physical column count of the sheet (maximum possible frames per row). */
    public int getTotalFrames() { return maxColumnsCount; }

    public int getTotalStates() { return rowsCount; }

    /**
     * Actual frame count for the given state row after transparent-frame trimming.
     *
     * @param state Row index (0-based).
     * @return Number of non-trailing-transparent frames; always >= 1.
     */
    public int getFrameCount(int state) {
        if (state < 0 || state >= rowsCount) return 1;
        return framesPerRow[state];
    }

    /**
     * A copy of the per-row frame counts, suitable for passing directly to
     * {@link SpriteSheetsAnimator#SpriteSheetsAnimator(double, int[])}.
     */
    public int[] getFramesPerRow() {
        return framesPerRow.clone();
    }

    /**
     * Returns the pre-sliced frame image, or {@code null} if coordinates are
     * out of bounds.
     */
    public Image getFrame(int state, int frame) {
        if (state < 0 || state >= rowsCount) return null;
        if (frame < 0 || frame >= framesPerRow[state]) return null;
        return slicedFrames[state][frame];
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Checks whether every pixel in the rectangle {@code (x, y, w, h)} has
     * alpha == 0.  Uses raw ARGB integers to avoid {@link javafx.scene.paint.Color}
     * allocation overhead (performance-critical during boot-time parsing).
     */
    private static boolean isRegionFullyTransparent(
            PixelReader px, int x, int y, int w, int h) {

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                // ARGB: top 8 bits are alpha
                int argb = px.getArgb(x + col, y + row);
                if ((argb >>> 24) != 0) return false;   // non-zero alpha → visible pixel
            }
        }
        return true;
    }
}