package uni.gaben.iscat.model.settings;

import de.androidpit.colorthief.ColorThief;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Componente algoritmico dedicato all'estrazione delle palette (MMCQ via ColorThief),
 * al calcolo delle luminanze relative WCAG e alla logica di bilanciamento dei canali cromatici.
 * <p>
 * Questa classe isola le funzioni matematiche e i filtri logici per la scomposizione e
 * l'assegnazione automatica dei colori estratti da file multimediali locali.
 */
public class PaletteExtractor {

    /**
     * Attiva o disattiva la Light Mode, aggiornando in modo asincrono le impostazioni nel database.
     * Ricalcola la tonalità cromatica dello sfondo in base alla disponibilità di un'immagine nel carosello
     * o applicando variazioni di luminosità HSB sull'accento primario corrente.
     *
     * @param model Il modello di stato reattivo del tema da aggiornare.
     * @param light {@code true} per impostare la modalità chiara, {@code false} per la modalità scura.
     */
    public static void setLightMode(ThemeSettingsModel model, boolean light) {
        model.lightModeProperty().set(light);

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int value = light ? 1 : 0;
            settings.setLightmode(value);
            IscatDB.getInstance().executeAsync(() -> {
                try {
                    IscatDB.getInstance().getSettingsDAO()
                            .updateThemeSetting(settings.getUserId(), "Lightmode", String.valueOf(value));
                } catch (Exception e) {
                    System.err.println("[ISCAT-ALG] Errore salvataggio light mode: " + e.getMessage());
                }
            });
        }

        boolean wasRainbowActive = model.rainbowModeProperty().get();
        if (wasRainbowActive) {
            model.isUpdatingProgrammatically = true;
        }

        if (!model.getCarouselImages().isEmpty()) {
            extractAndApplyPaletteInternal(model, model.getCarouselImages().get(model.currentImageIndexProperty().get()), wasRainbowActive);
        } else {
            Color cp = model.accentPrimaryProperty().get();
            model.bgPrimaryProperty().set(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, light ? 0.95 : 0.05));

            if (!wasRainbowActive) {
                model.applyThemeToManager();
            }
        }
        model.isUpdatingProgrammatically = false;
    }

    /**
     * Scompone e analizza l'immagine fornita tramite la libreria {@link ColorThief}. Genera la palette
     * di colori di riferimento e popola in modo sicuro il modello delegando l'assegnazione finale.
     *
     * @param model              Il modello di stato reattivo del tema su cui applicare le modifiche.
     * @param imageFile          Il puntatore al file immagine locale memorizzato su disco.
     * @param keepRainbowActive  Determina se preservare l'animazione ciclica corrente della Rainbow Mode.
     */
    public static void extractAndApplyPaletteInternal(ThemeSettingsModel model, File imageFile, boolean keepRainbowActive) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            if (!keepRainbowActive) {
                ThemeManager.getInstance().stopRainbowMode();
                model.stopRainbowSyncTimer();
                model.rainbowModeProperty().set(false);
            }

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, (int) IscatSettings.STANDARD_UNIT, 1, false);
            model.getWritablePalette().clear();
            for (int[] rgb : rawPalette) {
                model.getWritablePalette().add(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }

            assignColorsFromPalette(model);

            if (!keepRainbowActive) {
                model.applyThemeToManager();
            }
        } catch (IOException e) {
            System.err.println("[ISCAT-ALG] Errore lettura immagine: " + e.getMessage());
        }
    }

    /**
     * Isola ed ordina i colori memorizzati nella palette estratta del modello.
     * Seleziona lo sfondo ottimale calcolando la luminanza rispetto al contrasto corrente (Light/Dark Mode)
     * e distribuisce le restanti sfumature spettrali ai tre canali di accento.
     *
     * @param model Il modello di stato reattivo del tema contenente la palette da elaborare.
     */
    private static void assignColorsFromPalette(ThemeSettingsModel model) {
        List<Color> currentPalette = model.getWritablePalette();
        if (currentPalette.isEmpty()) return;

        Color bg = currentPalette.stream()
                .max(Comparator.comparingDouble(c -> model.lightModeProperty().get() ? luminance(c) : -luminance(c)))
                .orElse(Color.BLACK);

        List<Color> accents = currentPalette.stream()
                .filter(c -> !c.equals(bg))
                .toList();

        model.bgPrimaryProperty().set(bg);
        if (!accents.isEmpty()) model.accentPrimaryProperty().set(accents.get(0));
        if (accents.size() >= 2) model.accentSecondaryProperty().set(accents.get(1));
        if (accents.size() >= 3) model.accentTernaryProperty().set(accents.get(2));
    }

    /**
     * Determina il valore numerico della luminanza relativa standard associata ad un oggetto Color JavaFX
     * applicando i coefficienti spettrali standardizzati WCAG per i canali RGB.
     *
     * @param c L'istanza dell'oggetto {@link Color} da analizzare.
     * @return Il valore di luminanza calcolato espresso nell'intervallo standard [0.0, 1.0].
     */
    public static double luminance(Color c) {
        return 0.2126 * lin(c.getRed()) + 0.7152 * lin(c.getGreen()) + 0.0722 * lin(c.getBlue());
    }

    /**
     * Funzione di supporto interna per l'applicazione della linearizzazione sRGB (correzione di gamma inversa)
     * sui singoli canali cromatici.
     *
     * @param ch L'intensità normalizzata del singolo canale [0.0, 1.0].
     * @return Il valore del canale convertito in spazio lineare.
     */
    private static double lin(double ch) {
        return ch <= 0.03928 ? ch / 12.92 : Math.pow((ch + 0.055) / 1.055, 2.4);
    }

    /**
     * Converte un'istanza cromatica nativa in una stringa esadecimale standard a 24-bit compatibile con fogli di stile CSS.
     *
     * @param c L'istanza dell'oggetto {@link Color} da convertire.
     * @return Una stringa formattata contenente il codice esadecimale con prefisso hash (es: #FF00AA).
     */
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}