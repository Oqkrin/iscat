package uni.gaben.iscat.model.settings;

import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import uni.gaben.iscat.utils.theme.ThemeManager;
import java.io.File;
import java.util.*;

/**
 * Modello reattivo leggero per la gestione strutturale delle impostazioni del tema visivo dell'applicazione.
 * Mantiene lo stato dei colori di accento, dello sfondo, delle modalità grafiche attive
 * e dei puntatori ai file del carosello multimediale.
 * <p>
 * Questa classe funge da hub reattivo principale della UI, delegando la persistenza e i calcoli
 * algoritmici rispettivamente a {@link ThemePersistenceManager} e {@link PaletteExtractor}.
 */
public class ThemeSettingsModel {

    /** Proprietà dell'oggetto Color associata all'accento primario dell'applicazione. */
    private final ObjectProperty<Color> accentPrimary   = new SimpleObjectProperty<>(Color.WHITE);

    /** Proprietà dell'oggetto Color associata all'accento secondario dell'applicazione. */
    private final ObjectProperty<Color> accentSecondary = new SimpleObjectProperty<>(Color.WHITE);

    /** Proprietà dell'oggetto Color associata all'accento ternario dell'applicazione. */
    private final ObjectProperty<Color> accentTernary   = new SimpleObjectProperty<>(Color.WHITE);

    /** Proprietà dell'oggetto Color associata al colore dello sfondo principale. */
    private final ObjectProperty<Color> bgPrimary       = new SimpleObjectProperty<>(Color.BLACK);

    /** Proprietà booleana che traccia lo stato della modalità chiara (Light Mode). */
    private final BooleanProperty lightMode   = new SimpleBooleanProperty(false);

    /** Proprietà booleana che traccia lo stato dell'effetto dinamico arcobaleno (Rainbow Mode). */
    private final BooleanProperty rainbowMode = new SimpleBooleanProperty(false);

    /** Lista dinamica interna contenente la palette di colori estratta dall'immagine corrente. */
    private final List<Color> currentPalette = new ArrayList<>();

    /** Elenco ordinato dei file immagine caricati all'interno della memoria del carosello di sfondi. */
    private final List<File> carouselImages = new ArrayList<>();

    /** Proprietà intera che memorizza l'indice dell'immagine attualmente attiva nel carosello. */
    private final IntegerProperty currentImageIndex = new SimpleIntegerProperty(-1);

    /** Timer grafico JavaFX per sincronizzare l'aggiornamento dei colori in tempo reale durante la Rainbow Mode. */
    private AnimationTimer rainbowUiSyncTimer;

    /** Flag di controllo per inibire i listener interni durante aggiornamenti di stato pilotati via codice. */
    public boolean isUpdatingProgrammatically = false;

    /**
     * Costruttore della classe. Inizializza i listener di ascolto reattivi sulle quattro proprietà
     * cromatiche core per attivare la propagazione immediata delle modifiche verso il manager grafico.
     */
    public ThemeSettingsModel() {
        accentPrimary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentSecondary.addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentTernary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        bgPrimary      .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
    }

    // --- Proprietà ed Accessori JavaFX ---

    /** @return La proprietà legata all'accento cromatico primario. */
    public ObjectProperty<Color> accentPrimaryProperty()   { return accentPrimary; }

    /** @return La proprietà legata all'accento cromatico secondario. */
    public ObjectProperty<Color> accentSecondaryProperty() { return accentSecondary; }

    /** @return La proprietà legata all'accento cromatico ternario. */
    public ObjectProperty<Color> accentTernaryProperty()   { return accentTernary; }

    /** @return La proprietà legata al colore di sfondo primario. */
    public ObjectProperty<Color> bgPrimaryProperty()       { return bgPrimary; }

    /** @return La proprietà legata allo stato della Light Mode. */
    public BooleanProperty lightModeProperty()             { return lightMode; }

    /** @return La proprietà legata allo stato della Rainbow Mode. */
    public BooleanProperty rainbowModeProperty()           { return rainbowMode; }

    /** @return La proprietà contenente l'indice dell'immagine corrente nel carosello. */
    public IntegerProperty currentImageIndexProperty()     { return currentImageIndex; }

    /** @return Una vista non modificabile della lista contenente la palette estratta corrente. */
    public List<Color> getCurrentPalette() { return Collections.unmodifiableList(currentPalette); }

    /** @return Il riferimento diretto alla lista della palette modificabile, ad uso dei componenti interni. */
    public List<Color> getWritablePalette() { return currentPalette; }

    /** @return Il riferimento alla lista dei file immagine registrati nel sistema. */
    public List<File> getCarouselImages()  { return carouselImages; }

    /** @return Il conteggio totale degli elementi presenti all'interno del carosello di sfondi. */
    public int getCarouselSize()           { return carouselImages.size(); }

    /**
     * Recupera il file immagine puntato dall'indice corrente.
     *
     * @return L'oggetto {@link File} associato all'immagine attiva, o {@code null} se fuori dai limiti.
     */
    public File getCurrentImage() {
        int idx = currentImageIndex.get();
        return (idx >= 0 && idx < carouselImages.size()) ? carouselImages.get(idx) : null;
    }

    // --- Logica dello Stato e Sincronizzazione ---

    /**
     * Sincronizza istantaneamente lo stato cromatico del modello estraendo i valori correnti
     * configurati all'interno del singleton {@link ThemeManager}.
     */
    public void syncWithThemeManager() {
        ThemeManager tm = ThemeManager.getInstance();
        isUpdatingProgrammatically = true;
        accentPrimary.set(tm.getAccentPrimary());
        accentSecondary.set(tm.getAccentSecondary());
        accentTernary.set(tm.getAccentTertiary());
        bgPrimary.set(tm.getBgPrimary());
        isUpdatingProgrammatically = false;
    }

    /**
     * Interrompe la modalità Rainbow e forza il push statico dei colori correnti del modello
     * all'interno del motore di rendering CSS, avviando il salvataggio dei dati su DB.
     */
    public void applyThemeToManager() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.stopRainbowMode();
        stopRainbowSyncTimer();
        rainbowMode.set(false);

        String hexPrim = PaletteExtractor.toHex(accentPrimary.get());
        String hexSec  = PaletteExtractor.toHex(accentSecondary.get());
        String hexTer  = PaletteExtractor.toHex(accentTernary.get());
        String hexBg   = PaletteExtractor.toHex(bgPrimary.get());

        ThemePersistenceManager.saveThemeToDatabase(hexPrim, hexSec, hexTer, hexBg);
    }

    /**
     * Genera e avvia l'AnimationTimer adibito al campionamento a frequenza di aggiornamento schermo
     * dei canali dinamici emessi dal singleton del tema, iniettandoli protetti nella UI.
     */
    public void startRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) rainbowUiSyncTimer.stop();
        rainbowUiSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                isUpdatingProgrammatically = true;
                Color c = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.set(c);
                accentSecondary.set(c);
                accentTernary.set(c);
                isUpdatingProgrammatically = false;
            }
        };
        rainbowUiSyncTimer.start();
    }

    /**
     * Interrompe e distrugge in modo sicuro l'istanza attiva del timer grafico di campionamento.
     */
    public void stopRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) {
            rainbowUiSyncTimer.stop();
            rainbowUiSyncTimer = null;
        }
    }

    // --- Deleghe operative esterne ---

    /** Carica i dati cromatici e i flag utente dal DB delegando a {@link ThemePersistenceManager}. */
    public void loadFromDatabase() {
        ThemePersistenceManager.loadFromDatabase(this);
    }

    /**
     * Configura lo stato della modalità arcobaleno delegando a {@link ThemePersistenceManager}.
     *
     * @param active {@code true} per attivare gli effetti arcobaleno, {@code false} altrimenti.
     */
    public void setRainbowMode(boolean active) {
        ThemePersistenceManager.setRainbowMode(this, active);
    }

    /**
     * Modifica lo stato di rendering della modalità chiara/scura delegando a {@link PaletteExtractor}.
     *
     * @param light {@code true} per abilitare la Light Mode, {@code false} per la Dark Mode.
     */
    public void setLightMode(boolean light) {
        PaletteExtractor.setLightMode(this, light);
    }

    /**
     * Inserisce un nuovo file immagine nel carosello e ne estrae la palette cromatica associata.
     *
     * @param imageFile Il file immagine locale da includere e campionare.
     */
    public void addImageAndApply(File imageFile) {
        carouselImages.add(imageFile);
        currentImageIndex.set(carouselImages.size() - 1);
        PaletteExtractor.extractAndApplyPaletteInternal(this, imageFile, false);
    }

    /**
     * Scorre ciclicamente l'indice del carosello di sfondi caricati in memoria.
     *
     * @param next {@code true} per avanzare al file successivo, {@code false} per tornare al precedente.
     */
    public void navigateCarousel(boolean next) {
        if (carouselImages.isEmpty()) return;
        int idx = currentImageIndex.get();
        int newIdx = next ? (idx + 1) % carouselImages.size() : (idx - 1 + carouselImages.size()) % carouselImages.size();
        currentImageIndex.set(newIdx);
        PaletteExtractor.extractAndApplyPaletteInternal(this, carouselImages.get(newIdx), false);
    }

    /** Esegue il ripristino di fabbrica di tutti i colori e parametri delegando a {@link ThemePersistenceManager}. */
    public void restoreDefaultTheme() {
        ThemePersistenceManager.restoreDefaultTheme(this);
    }

    // --- Metodi statici mantenuti per retrocompatibilità con il controller ---

    /**
     * Calcola la luminanza relativa standard di un colore. Mantenuto per retrocompatibilità.
     *
     * @param c L'oggetto {@link Color} da analizzare.
     * @return Valore di luminanza compreso nell'intervallo [0.0, 1.0].
     */
    public static double luminance(Color c) { return PaletteExtractor.luminance(c); }

    /**
     * Trasforma un oggetto Color in stringa esadecimale. Mantenuto per retrocompatibilità.
     *
     * @param c L'oggetto {@link Color} da elaborare.
     * @return Stringa CSS esadecimale formattata (es: #FFFFFF).
     */
    public static String toHex(Color c) { return PaletteExtractor.toHex(c); }
}