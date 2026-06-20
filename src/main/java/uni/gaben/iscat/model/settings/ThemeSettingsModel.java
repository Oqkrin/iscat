package uni.gaben.iscat.model.settings;

import de.androidpit.colorthief.ColorThief;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
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
 * Modello per la gestione delle impostazioni del tema visivo dell'applicazione.
 * Mantiene lo stato dei colori di accento, dello sfondo, delle modalità grafiche
 * (Light Mode e Rainbow Mode) e del carosello di immagini per l'estrazione delle palette.
 */
public class ThemeSettingsModel {

    /** Proprietà dell'oggetto Color associata all'accento primario. */
    private final ObjectProperty<Color> accentPrimary   = new SimpleObjectProperty<>(Color.WHITE);
    /** Proprietà dell'oggetto Color associata all'accento secondario. */
    private final ObjectProperty<Color> accentSecondary = new SimpleObjectProperty<>(Color.WHITE);
    /** Proprietà dell'oggetto Color associata all'accento ternario. */
    private final ObjectProperty<Color> accentTernary   = new SimpleObjectProperty<>(Color.WHITE);
    /** Proprietà dell'oggetto Color associata allo sfondo principale. */
    private final ObjectProperty<Color> bgPrimary       = new SimpleObjectProperty<>(Color.BLACK);

    /** Proprietà booleana che traccia l'attivazione della modalità chiara (Light Mode). */
    private final BooleanProperty lightMode   = new SimpleBooleanProperty(false);
    /** Proprietà booleana che traccia l'attivazione dell'effetto arcobaleno (Rainbow Mode). */
    private final BooleanProperty rainbowMode = new SimpleBooleanProperty(false);

    /** Lista dei colori estratti dall'immagine correntemente selezionata. */
    private final List<Color> currentPalette = new ArrayList<>();
    /** Elenco dei file immagine caricati nel sistema carosello dello sfondo. */
    private final List<File> carouselImages = new ArrayList<>();
    /** Proprietà intera che memorizza l'indice dell'immagine correntemente attiva. */
    private final IntegerProperty currentImageIndex = new SimpleIntegerProperty(-1);

    /** Timer grafico per sincronizzare ciclicamente la UI ai cambi di colore della Rainbow Mode. */
    private AnimationTimer rainbowUiSyncTimer;
    /** Flag utilizzato per disabilitare i listener reattivi durante aggiornamenti controllati dal codice. */
    private boolean isUpdatingProgrammatically = false;

    /**
     * Costruttore della classe. Configura i listener di aggiornamento sulle proprietà
     * dei quattro colori del tema principale.
     */
    public ThemeSettingsModel() {
        accentPrimary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentSecondary.addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentTernary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        bgPrimary      .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
    }

    /**
     * @return La proprietà legata all'accento cromatico primario.
     */
    public ObjectProperty<Color> accentPrimaryProperty()   { return accentPrimary; }

    /**
     * @return La proprietà legata all'accento cromatico secondario.
     */
    public ObjectProperty<Color> accentSecondaryProperty() { return accentSecondary; }

    /**
     * @return La proprietà legata all'accento cromatico ternario.
     */
    public ObjectProperty<Color> accentTernaryProperty()   { return accentTernary; }

    /**
     * @return La proprietà legata al colore di sfondo primario.
     */
    public ObjectProperty<Color> bgPrimaryProperty()       { return bgPrimary; }

    /**
     * @return La proprietà legata allo stato della Light Mode.
     */
    public BooleanProperty lightModeProperty()   { return lightMode; }

    /**
     * @return La proprietà legata allo stato della Rainbow Mode.
     */
    public BooleanProperty rainbowModeProperty() { return rainbowMode; }

    /**
     * @return La proprietà contenente l'indice dell'immagine corrente nel carosello.
     */
    public IntegerProperty currentImageIndexProperty() { return currentImageIndex; }

    /**
     * @return Una vista non modificabile della lista contenente la palette estratta corrente.
     */
    public List<Color> getCurrentPalette() { return Collections.unmodifiableList(currentPalette); }

    /**
     * Recupera i dati del tema registrati per l'utente corrente dal database,
     * configurando di conseguenza le proprietà interne di questo modello.
     */
    public void loadFromDatabase() {
        SessionManager session = SessionManager.getInstance();
        UserSettings settings = session.getCurrentSettings();
        if (settings == null) return;

        boolean dbLight   = settings.getLightmode() == 1;
        boolean dbRainbow = settings.getRainbowMode() == 1;

        isUpdatingProgrammatically = true;

        lightMode.set(dbLight);
        rainbowMode.set(dbRainbow);

        if (settings.getPrimaryTheme() != null && !"#FFFFFF".equalsIgnoreCase(settings.getPrimaryTheme())) {
            accentPrimary.set(Color.web(settings.getPrimaryTheme()));
            accentSecondary.set(Color.web(settings.getSecondaryTheme()));
            accentTernary.set(Color.web(settings.getTertiaryTheme()));
            bgPrimary.set(Color.web(settings.getBackgroundTheme()));
        } else {
            ThemeManager tm = ThemeManager.getInstance();
            accentPrimary.set(tm.getAccentPrimary());
            accentSecondary.set(tm.getAccentSecondary());
            accentTernary.set(tm.getAccentTernary());
            bgPrimary.set(tm.getBgPrimary());
        }

        isUpdatingProgrammatically = false;
    }

    /**
     * Forza l'aggiornamento immediato delle proprietà del modello estraendo i valori
     * correnti configurati nell'istanza singleton di ThemeManager.
     */
    public void syncWithThemeManager() {
        ThemeManager tm = ThemeManager.getInstance();
        isUpdatingProgrammatically = true;
        accentPrimary.set(tm.getAccentPrimary());
        accentSecondary.set(tm.getAccentSecondary());
        accentTernary.set(tm.getAccentTernary());
        bgPrimary.set(tm.getBgPrimary());
        isUpdatingProgrammatically = false;
    }

    /**
     * Interrompe l'effetto Rainbow e applica staticamente la configurazione cromatica
     * corrente al ThemeManager, avviando la persistenza dei dati esadecimali nel database.
     */
    public void applyThemeToManager() {
        // Se stiamo modificando i colori a mano, allora sì: fermiamo il rainbow
        ThemeManager tm = ThemeManager.getInstance();
        tm.stopRainbowMode();
        stopRainbowSyncTimer();
        rainbowMode.set(false);

        String hexPrim = toHex(accentPrimary.get());
        String hexSec  = toHex(accentSecondary.get());
        String hexTer  = toHex(accentTernary.get());
        String hexBg   = toHex(bgPrimary.get());

        saveThemeToDatabase(hexPrim, hexSec, hexTer, hexBg);
    }

    /**
     * Registra in via asincrona i codici colore esadecimali forniti all'interno delle tabelle utente.
     * * @param hexPrim Colore primario in formato esadecimale stringa.
     * @param hexSec  Colore secondario in formato esadecimale stringa.
     * @param hexTer  Colore ternario in formato esadecimale stringa.
     * @param hexBg   Colore dello sfondo in formato esadecimale stringa.
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
     * Modifica lo stato di attivazione della Rainbow Mode, attivando i relativi effetti audio
     * e salvando la preferenza dell'utente nel database.
     * * @param active {@code true} se la Rainbow Mode deve essere abilitata, {@code false} altrimenti.
     */
    public void setRainbowMode(boolean active) {
        rainbowMode.set(active);
        if (active) {
            AudioManager.getInstance().playSFX("rainbow");
        } else {
            AudioManager.getInstance().playSFX("laugh");
            syncWithThemeManager();
        }

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
     * Attiva o disattiva la Light Mode, aggiornando le impostazioni nel database.
     * Se la Rainbow Mode risulta attiva al momento dello switch, l'aggiornamento dei colori
     * avviene in modalità protetta per preservare l'animazione dinamica dell'interfaccia.
     * * @param light {@code true} per impostare la modalità chiara, {@code false} per la scura.
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

        boolean wasRainbowActive = rainbowMode.get();
        if (wasRainbowActive) {
            isUpdatingProgrammatically = true;
        }

        if (!carouselImages.isEmpty()) {
            extractAndApplyPaletteInternal(carouselImages.get(currentImageIndex.get()), wasRainbowActive);
        } else {
            Color cp = accentPrimary.get();
            bgPrimary.set(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, light ? 0.95 : 0.05));

            if (!wasRainbowActive) {
                applyThemeToManager();
            }
        }

        isUpdatingProgrammatically = false;
    }


    /**
     * Integra un file immagine all'interno della lista del carosello e ne estrae
     * automaticamente i colori dominanti impostandoli come tema attivo.
     * * @param imageFile Il puntatore al file immagine da processare.
     */
    public void addImageAndApply(File imageFile) {
        carouselImages.add(imageFile);
        currentImageIndex.set(carouselImages.size() - 1);
        extractAndApplyPalette(imageFile);
    }

    /**
     * Scorre l'indice del carosello delle immagini caricate ed esegue l'estrazione
     * cromatico-visiva sul nuovo elemento selezionato.
     * * @param next {@code true} se si vuole avanzare all'immagine successiva, {@code false} per la precedente.
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
     * Avvia il processo standard di estrazione della tavolozza colori forzando la chiusura
     * del comportamento dinamico della Rainbow Mode.
     * * @param imageFile Il file sorgente da analizzare.
     */
    private void extractAndApplyPalette(File imageFile) {
        extractAndApplyPaletteInternal(imageFile, false);
    }

    /**
     * Scompone e analizza l'immagine fornita tramite la libreria ColorThief. Genera la palette
     * di colori di riferimento e gestisce se mantenere l'effetto arcobaleno in esecuzione o meno.
     * * @param imageFile          Il file immagine su disco.
     * @param keepRainbowActive  Determina se mantenere attiva l'animazione della Rainbow Mode.
     */
    private void extractAndApplyPaletteInternal(File imageFile, boolean keepRainbowActive) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            if (!keepRainbowActive) {
                ThemeManager.getInstance().stopRainbowMode();
                stopRainbowSyncTimer();
                rainbowMode.set(false);
            }

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, (int) IscatSettings.STANDARD_UNIT, 1, false);
            currentPalette.clear();
            for (int[] rgb : rawPalette) {
                currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }

            assignColorsFromPalette();

            if (!keepRainbowActive) {
                applyThemeToManager();
            }
        } catch (IOException e) {
            System.err.println("[ISCAT] Errore lettura immagine: " + e.getMessage());
        }
    }

    /**
     * Seleziona ed ordina i colori inseriti nella palette estratta correntemente, impostando
     * lo sfondo ottimale in base alla Light/Dark Mode e distribuendo i restanti colori agli accenti.
     */
    private void assignColorsFromPalette() {
        if (currentPalette.isEmpty()) return;

        Color bg = currentPalette.stream()
                .max(Comparator.comparingDouble(c -> lightMode.get() ? luminance(c) : -luminance(c)))
                .orElse(Color.BLACK);

        List<Color> accents = currentPalette.stream()
                .filter(c -> !c.equals(bg))
                .toList();

        bgPrimary.set(bg);
        if (!accents.isEmpty()) accentPrimary.set(accents.get(0));
        if (accents.size() >= 2) accentSecondary.set(accents.get(1));
        if (accents.size() >= 3) accentTernary.set(accents.get(2));
    }


    /**
     * Crea e avvia l'AnimationTimer dedicato al campionamento in tempo reale dei colori dinamici
     * generati dal ThemeManager, trasferendoli protetti alle rispettive proprietà FX.
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
     * Ferma ed elimina l'istanza attiva dell'AnimationTimer per la sincronizzazione del Rainbow.
     */
    public void stopRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) {
            rainbowUiSyncTimer.stop();
            rainbowUiSyncTimer = null;
        }
    }

    /**
     * Resetta in maniera centralizzata tutte le preferenze di personalizzazione ai valori di fabbrica.
     * Ripristina i flag booleani, cancella le immagini temporanee in memoria e applica l'aggiornamento
     * massivo sul database relazionale per l'utente loggato.
     */
    public void restoreDefaultTheme() {
        stopRainbowSyncTimer();

        // Blocca i listener di aggiornamento temporaneamente
        isUpdatingProgrammatically = true;

        // Reset dei flag di stato
        lightMode.set(false);
        rainbowMode.set(false);
        carouselImages.clear();
        currentPalette.clear();
        currentImageIndex.set(-1);

        // Impostiamo i colori standard originali (fai combaciare questi hex coi tuoi predefiniti)
        Color defaultPrimary = Color.web("#cbcbcb");
        Color defaultSecondary = Color.web("#a9a9a9");
        Color defaultTertiary = Color.web("#333333");
        Color defaultBg = Color.web("#010203");

        accentPrimary.set(defaultPrimary);
        accentSecondary.set(defaultSecondary);
        accentTernary.set(defaultTertiary);
        bgPrimary.set(defaultBg);

        // Sblocca i listener
        isUpdatingProgrammatically = false;

        // Persistenza istantanea di tutti i parametri di ripristino sul Database SQL/Sessione
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            settings.setLightmode(0);
            settings.setRainbowMode(0);
            settings.setPrimaryTheme(toHex(defaultPrimary));
            settings.setSecondaryTheme(toHex(defaultSecondary));
            settings.setTertiaryTheme(toHex(defaultTertiary));
            settings.setBackgroundTheme(toHex(defaultBg));

            IscatDB.getInstance().executeAsync(() -> {
                try {
                    SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                    dao.updateThemeSetting(settings.getUserId(), "Lightmode", "0");
                    dao.updateThemeSetting(settings.getUserId(), "RainbowMode", "0");
                    dao.updateThemeSetting(settings.getUserId(), "PrimaryTheme", toHex(defaultPrimary));
                    dao.updateThemeSetting(settings.getUserId(), "SecondaryTheme", toHex(defaultSecondary));
                    dao.updateThemeSetting(settings.getUserId(), "TertiaryTheme", toHex(defaultTertiary));
                    dao.updateThemeSetting(settings.getUserId(), "BackgroundTheme", toHex(defaultBg));
                } catch (Exception e) {
                    System.err.println("[ISCAT] Errore durante il restore dei dati nel DB: " + e.getMessage());
                }
            });
        }
    }


    /**
     * Determina il valore numerico di luminanza relativa standard associato ad un colore specifico.
     * * @param c Il colore di input da analizzare.
     * @return Rappresentazione della luminanza in valore compreso nel range [0.0, 1.0].
     */
    public static double luminance(Color c) {
        return 0.2126 * lin(c.getRed()) + 0.7152 * lin(c.getGreen()) + 0.0722 * lin(c.getBlue());
    }

    /**
     * Funzione helper per l'applicazione della correzione di gamma lineare sui canali di colore.
     * * @param ch Intensità del canale cromatico.
     * @return Il valore linearizzato del canale.
     */
    private static double lin(double ch) {
        return ch <= 0.03928 ? ch / 12.92 : Math.pow((ch + 0.055) / 1.055, 2.4);
    }

    /**
     * Converte l'istanza nativa del colore fornito in formato stringa esadecimale CSS compatibile.
     * * @param c L'oggetto Color JavaFX da analizzare.
     * @return Stringa formattata nel formato esadecimale standard (es: #FFAA00).
     */
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    /**
     * @return Il conteggio totale dei file immagine registrati nell'array del carosello.
     */
    public int getCarouselSize() { return carouselImages.size(); }

    /**
     * @return L'istanza dell'oggetto File associato all'indice correntemente selezionato,
     * oppure {@code null} se l'indice esce dai limiti strutturali.
     */
    public File getCurrentImage() {
        int idx = currentImageIndex.get();
        return (idx >= 0 && idx < carouselImages.size()) ? carouselImages.get(idx) : null;
    }
}