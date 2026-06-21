package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.InputStream;

/**
 * Parser intelligente per la scomposizione (slicing) di uno sprite sheet.
 * Divide l'immagine in una griglia bidimensionale in base alle dimensioni del frame
 * e ottimizza la memoria calcolando automaticamente il numero reale di frame non vuoti
 * presenti su ogni riga (stato) tramite analisi della trasparenza.
 */
public class SpriteSheetsParser {

    private final Image sheet;
    public final int frameWidth;
    public final int frameHeight;

    private final int maxColumnsCount;
    private final int rowsCount;
    private final int[] framesPerRow;
    private final Image[][] slicedFrames;

    /**
     * Costruisce il parser ed esegue immediatamente lo slicing dello sprite sheet
     * partendo da un {@link InputStream} generico.
     *
     * @param imageStream Il flusso dati dell'immagine (es. da file esterno o classpath).
     * @param frameWidth  Larghezza in pixel di un singolo frame.
     * @param frameHeight Altezza in pixel di un singolo frame.
     */
    public SpriteSheetsParser(InputStream imageStream, int frameWidth, int frameHeight) {
        this.sheet = new Image(imageStream);
        this.frameWidth  = frameWidth;
        this.frameHeight = frameHeight;

        this.maxColumnsCount = Math.max(1, (int) sheet.getWidth()  / frameWidth);
        this.rowsCount       = Math.max(1, (int) sheet.getHeight() / frameHeight);

        PixelReader px = sheet.getPixelReader();

        this.framesPerRow = new int[rowsCount];
        this.slicedFrames = new Image[rowsCount][];

        // Analizza ogni riga dello sprite sheet
        for (int r = 0; r < rowsCount; r++) {
            int lastNonTransparent = 0;

            // Scansione a ritroso dalle colonne per identificare l'ultimo frame reale (non vuoto)
            for (int c = maxColumnsCount - 1; c >= 0; c--) {
                if (!isRegionFullyTransparent(px, c * frameWidth, r * frameHeight, frameWidth, frameHeight)) {
                    lastNonTransparent = c;
                    break;
                }
            }
            int count = lastNonTransparent + 1;
            framesPerRow[r] = count;

            // Inizializza l'array frastagliato e ritaglia le sub-immagini operative
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
    public int getTotalFrames() { return maxColumnsCount; }
    public int getTotalStates() { return rowsCount; }

    /**
     * Restituisce una copia dell'array contenente il numero di frame validi per ciascuna riga.
     * Sicuro contro manipolazioni esterne grazie alla clonazione.
     */
    public int[] getFramesPerRow() { return framesPerRow.clone(); }

    /**
     * Recupera lo specifico frame di animazione ritagliato.
     * * @param state Indice dello stato corrente (riga dello sprite sheet).
     * @param frame Indice del frame richiesto (colonna).
     * @return L'istanza dell'{@link Image} corrispondente, o {@code null} se gli indici sono fuori dai limiti.
     */
    public Image getFrame(int state, int frame) {
        if (state < 0 || state >= rowsCount) return null;
        if (frame < 0 || frame >= framesPerRow[state]) return null;
        return slicedFrames[state][frame];
    }

    /**
     * Analizza la regione rettangolare del PixelReader per verificare se è interamente trasparente.
     */
    private static boolean isRegionFullyTransparent(PixelReader px, int x, int y, int w, int h) {
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                // Estrae il canale Alpha spostando i bit ARGB ed effettua il controllo di presenza colore
                if ((px.getArgb(x + col, y + row) >>> 24) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}