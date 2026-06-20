package uni.gaben.iscat.model.settings;

import de.androidpit.colorthief.ColorThief;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Modello che incapsula tutta la logica di business e lo stato relativo ai temi dell'applicazione.
 * Gestisce i colori personalizzati, le palette estratte da immagini, la modalità arcobaleno,
 * il salvataggio su database e la sincronizzazione con {@link ThemeManager}.
 *
 * <p>Il controller delle impostazioni tema ({@code ThemeSettingsController}) si limita
 * a collegare la UI a questo modello, senza conoscere i dettagli di persistenza o estrazione colori.</p>
 *
 * @author Gabriele (Gaben)
 */
public class ThemeSettingsModel {

    private final ObjectProperty<Color> accentPrimary   = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> accentSecondary = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> accentTernary   = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> bgPrimary       = new SimpleObjectProperty<>(Color.BLACK);

    private final BooleanProperty lightMode   = new SimpleBooleanProperty(false);
    private final BooleanProperty rainbowMode = new SimpleBooleanProperty(false);

    private final List<Color> currentPalette = new ArrayList<>();
    // Carosello di immagini caricate
    private final List<File> carouselImages = new ArrayList<>();
    private final IntegerProperty currentImageIndex = new SimpleIntegerProperty(-1);

    // Timer per mantenere i picker aggiornati in modalità arcobaleno
    private AnimationTimer rainbowUiSyncTimer;

    /**
     * Crea un nuovo modello dei temi. Non carica dati; usa {@link #loadFromDatabase()}
     * per popolare lo stato dal database o dalla sessione corrente.
     */
    public ThemeSettingsModel() {
        accentPrimary  .addListener((obs, old, val) -> applyThemeToManager());
        accentSecondary.addListener((obs, old, val) -> applyThemeToManager());
        accentTernary  .addListener((obs, old, val) -> applyThemeToManager());
        bgPrimary      .addListener((obs, old, val) -> applyThemeToManager());
    }

    public ObjectProperty<Color> accentPrimaryProperty()   { return accentPrimary; }
    public ObjectProperty<Color> accentSecondaryProperty() { return accentSecondary; }
    public ObjectProperty<Color> accentTernaryProperty()   { return accentTernary; }
    public ObjectProperty<Color> bgPrimaryProperty()       { return bgPrimary; }

    public BooleanProperty lightModeProperty()   { return lightMode; }
    public BooleanProperty rainbowModeProperty() { return rainbowMode; }

    public IntegerProperty currentImageIndexProperty() { return currentImageIndex; }
    public List<Color> getCurrentPalette() { return Collections.unmodifiableList(currentPalette); }


    /**
     * Carica le impostazioni tema dal database e aggiorna tutte le proprietà del modello.
     * Va chiamato dopo che la scena è disponibile (per applicare i colori).
     */
    public void loadFromDatabase() {
        SessionManager session = SessionManager.getInstance();
        UserSettings settings = session.getCurrentSettings();
        if (settings == null) return;

        boolean dbLight   = settings.getLightmode() == 1;
        boolean dbRainbow = settings.getRainbowMode() == 1;

        lightMode.set(dbLight);
        rainbowMode.set(dbRainbow);

        if (settings.getPrimaryTheme() != null && !"#FFFFFF".equalsIgnoreCase(settings.getPrimaryTheme())) {
            accentPrimary.set(Color.web(settings.getPrimaryTheme()));
            accentSecondary.set(Color.web(settings.getSecondaryTheme()));
            accentTernary.set(Color.web(settings.getTertiaryTheme()));
            bgPrimary.set(Color.web(settings.getBackgroundTheme()));
        } else {
            syncWithThemeManager();
        }

        // Se rainbow era attivo, lo riattiviamo (ma ThemeManager va avviato dalla UI quando ha la scena)
    }

    /**
     * Sincronizza i valori delle proprietà con lo stato corrente di {@link ThemeManager}.
     */
    public void syncWithThemeManager() {
        ThemeManager tm = ThemeManager.getInstance();
        accentPrimary.set(tm.getAccentPrimary());
        accentSecondary.set(tm.getAccentSecondary());
        accentTernary.set(tm.getAccentTernary());
        bgPrimary.set(tm.getBgPrimary());
    }

    /**
     * Applica i colori attuali al {@link ThemeManager} e li salva nel database.
     * Deve essere chiamato dopo ogni modifica manuale (o da picker).
     */
    public void applyThemeToManager() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.stopRainbowMode();
        stopRainbowSyncTimer();
        rainbowMode.set(false);

        String hexPrim = toHex(accentPrimary.get());
        String hexSec  = toHex(accentSecondary.get());
        String hexTer  = toHex(accentTernary.get());
        String hexBg   = toHex(bgPrimary.get());

        // Aggiorna ThemeManager (richiede la scena per applicare i CSS, ma il modello non ha la scena:
        // sarà compito del controller chiamare ThemeManager.applyHexColorsTheme con la scena effettiva).
        // Qui lasciamo che il controller gestisca l'applicazione visiva; il modello si limita a salvare.

        saveThemeToDatabase(hexPrim, hexSec, hexTer, hexBg);
    }

    /**
     * Salva i quattro colori tema sul database in modo asincrono.
     */
    private void saveThemeToDatabase(String hexPrim, String hexSec, String hexTer, String hexBg) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;

        settings.setPrimaryTheme(hexPrim);
        settings.setSecondaryTheme(hexSec);
        settings.setTertiaryTheme(hexTer);
        settings.setBackgroundTheme(hexBg);

        IscatDB.getInstance().executeAsync(() -> {
            try {
                SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                dao.updateThemeSetting(settings.getUserId(), "PrimaryTheme", hexPrim);
                dao.updateThemeSetting(settings.getUserId(), "SecondaryTheme", hexSec);
                dao.updateThemeSetting(settings.getUserId(), "TertiaryTheme", hexTer);
                dao.updateThemeSetting(settings.getUserId(), "BackgroundTheme", hexBg);
            } catch (Exception e) {
                System.err.println("[ISCAT] Errore salvataggio colori: " + e.getMessage());
            }
        });
    }

    /**
     * Attiva/disattiva la modalità arcobaleno e salva la scelta.
     * @param active {@code true} per attivare, {@code false} per disattivare.
     */
    public void setRainbowMode(boolean active) {
        rainbowMode.set(active);
        if (active) {
            AudioManager.getInstance().playSFX("rainbow");
        } else {
            AudioManager.getInstance().playSFX("laugh");
            syncWithThemeManager();
        }

        // Salva nel DB
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int value = active ? 1 : 0;
            settings.setRainbowMode(value);
            IscatDB.getInstance().executeAsync(() -> {
                try {
                    IscatDB.getInstance().getSettingsDAO()
                            .updateThemeSetting(settings.getUserId(), "RainbowMode", String.valueOf(value));
                } catch (Exception e) {
                    System.err.println("[ISCAT] Errore salvataggio rainbow: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Attiva/disattiva la modalità light e salva la scelta.
     * @param light {@code true} per tema chiaro, {@code false} per scuro.
     */
    public void setLightMode(boolean light) {
        lightMode.set(light);
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int value = light ? 1 : 0;
            settings.setLightmode(value);
            IscatDB.getInstance().executeAsync(() -> {
                try {
                    IscatDB.getInstance().getSettingsDAO()
                            .updateThemeSetting(settings.getUserId(), "Lightmode", String.valueOf(value));
                } catch (Exception e) {
                    System.err.println("[ISCAT] Errore salvataggio light mode: " + e.getMessage());
                }
            });
        }
        // Ricalcola i colori da palette o da immagine corrente
        if (!carouselImages.isEmpty()) {
            extractAndApplyPalette(carouselImages.get(currentImageIndex.get()));
        } else {
            // Semplice inversione luminosità sfondo
            Color cp = accentPrimary.get();
            bgPrimary.set(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, light ? 0.95 : 0.05));
            applyThemeToManager();
        }
    }

    /* =================== Estrazione palette da immagini =================== */

    /**
     * Aggiunge un'immagine al carosello ed estrae la palette di colori.
     * @param imageFile file immagine da cui estrarre i colori dominanti.
     */
    public void addImageAndApply(File imageFile) {
        carouselImages.add(imageFile);
        currentImageIndex.set(carouselImages.size() - 1);
        extractAndApplyPalette(imageFile);
    }

    /**
     * Passa all'immagine successiva/precedente nel carosello.
     * @param next {@code true} per andare avanti, {@code false} per indietro.
     */
    public void navigateCarousel(boolean next) {
        if (carouselImages.isEmpty()) return;
        int idx = currentImageIndex.get();
        int newIdx = next ? (idx + 1) % carouselImages.size()
                : (idx - 1 + carouselImages.size()) % carouselImages.size();
        currentImageIndex.set(newIdx);
        extractAndApplyPalette(carouselImages.get(newIdx));
    }

    /**
     * Estrae la palette dall'immagine data, aggiorna i colori del modello e
     * notifica i cambiamenti. Salva automaticamente i nuovi colori.
     */
    private void extractAndApplyPalette(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            // Disattiva rainbow se attivo
            ThemeManager.getInstance().stopRainbowMode();
            stopRainbowSyncTimer();
            rainbowMode.set(false);

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, (int) IscatSettings.STANDARD_UNIT, 1, false);
            currentPalette.clear();
            for (int[] rgb : rawPalette) {
                currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }

            // Scegli i colori da assegnare ai picker
            assignColorsFromPalette();
            // Dopo l'assegnazione, applyThemeToManager() viene chiamato dai listener delle proprietà
        } catch (IOException e) {
            System.err.println("[ISCAT] Errore lettura immagine: " + e.getMessage());
        }
    }

    /**
     * Distribuisce i colori della palette corrente nei quattro picker,
     * basandosi sulla modalità light/dark per lo sfondo.
     */
    private void assignColorsFromPalette() {
        if (currentPalette.isEmpty()) return;

        // Scegli lo sfondo come il colore più chiaro/scuro a seconda della modalità
        Color bg = currentPalette.stream()
                .max(Comparator.comparingDouble(c -> lightMode.get() ? luminance(c) : -luminance(c)))
                .orElse(Color.BLACK);

        // Accenti: tutti gli altri colori
        List<Color> accents = currentPalette.stream()
                .filter(c -> !c.equals(bg))
                .toList();

        // Per evitare trigger programmatici (loop) disabilitiamo temporaneamente l'aggiornamento?
        // Non serve perché i listener scatenano solo applyThemeToManager che è già sotto controllo.
        bgPrimary.set(bg);
        if (!accents.isEmpty()) accentPrimary.set(accents.get(0));
        if (accents.size() >= 2) accentSecondary.set(accents.get(1));
        if (accents.size() >= 3) accentTernary.set(accents.get(2));
    }

    /* =================== Timer sincronizzazione UI arcobaleno =================== */

    /**
     * Avvia un timer che aggiorna continuamente i colori dei picker per riflettere
     * l'arcobaleno dinamico.
     */
    public void startRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) rainbowUiSyncTimer.stop();
        rainbowUiSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                Color c = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.set(c);
                accentSecondary.set(c);
                accentTernary.set(c);
            }
        };
        rainbowUiSyncTimer.start();
    }

    /**
     * Ferma il timer di sincronizzazione arcobaleno.
     */
    public void stopRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) {
            rainbowUiSyncTimer.stop();
            rainbowUiSyncTimer = null;
        }
    }

    /* =================== Metodi di utilità =================== */

    /**
     * Calcola la luminanza relativa di un colore secondo lo standard sRGB.
     * @param c colore JavaFX.
     * @return valore tra 0 e 1.
     */
    public static double luminance(Color c) {
        return 0.2126 * lin(c.getRed()) + 0.7152 * lin(c.getGreen()) + 0.0722 * lin(c.getBlue());
    }

    private static double lin(double ch) {
        return ch <= 0.03928 ? ch / 12.92 : Math.pow((ch + 0.055) / 1.055, 2.4);
    }

    /**
     * Converte un colore JavaFX nel formato esadecimale {@code #rrggbb}.
     */
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    /**
     * Restituisce il numero di immagini nel carosello.
     */
    public int getCarouselSize() {
        return carouselImages.size();
    }

    /**
     * Restituisce l'immagine corrente (se presente) altrimenti {@code null}.
     */
    public File getCurrentImage() {
        int idx = currentImageIndex.get();
        return (idx >= 0 && idx < carouselImages.size()) ? carouselImages.get(idx) : null;
    }
}