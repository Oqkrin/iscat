package uni.gaben.iscat.model.settings;

import javafx.scene.paint.Color;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Gestore delegato alle operazioni di input/output, persistenza asincrona
 * e ripristino delle impostazioni del tema sul database.
 * <p>
 * Questa classe isola completamente l'accesso ai layer di persistenza (DAO, DB)
 * e la gestione dello stato della sessione utente per le preferenze grafiche.
 */
public class ThemePersistenceManager {

    /**
     * Recupera le impostazioni del tema salvate per l'utente corrente dal database della sessione
     * e le applica in modo controllato alle proprietà reattive del modello fornito.
     *
     * @param model Il modello di stato reattivo del tema da popolare con i dati estratti.
     */
    public static void loadFromDatabase(ThemeSettingsModel model) {
        SessionManager session = SessionManager.getInstance();
        UserSettings settings = session.getCurrentSettings();
        if (settings == null) return;

        boolean dbLight   = settings.getLightmode() == 1;
        boolean dbRainbow = settings.getRainbowMode() == 1;

        model.isUpdatingProgrammatically = true;
        model.lightModeProperty().set(dbLight);
        model.rainbowModeProperty().set(dbRainbow);

        if (settings.getPrimaryTheme() != null && !"#FFFFFF".equalsIgnoreCase(settings.getPrimaryTheme())) {
            model.accentPrimaryProperty().set(Color.web(settings.getPrimaryTheme()));
            model.accentSecondaryProperty().set(Color.web(settings.getSecondaryTheme()));
            model.accentTernaryProperty().set(Color.web(settings.getTertiaryTheme()));
            model.bgPrimaryProperty().set(Color.web(settings.getBackgroundTheme()));
        } else {
            ThemeManager tm = ThemeManager.getInstance();
            model.accentPrimaryProperty().set(tm.getAccentPrimary());
            model.accentSecondaryProperty().set(tm.getAccentSecondary());
            model.accentTernaryProperty().set(tm.getAccentTernary());
            model.bgPrimaryProperty().set(tm.getBgPrimary());
        }
        model.isUpdatingProgrammatically = false;
    }

    /**
     * Registra in via asincrona sul database relazionale e all'interno della sessione utente
     * i codici esadecimali correnti associati alla tavolozza dei colori dell'interfaccia.
     *
     * @param hexPrim Codice esadecimale stringa dell'accento primario (es: #FFAA00).
     * @param hexSec  Codice esadecimale stringa dell'accento secondario.
     * @param hexTer  Codice esadecimale stringa dell'accento ternario.
     * @param hexBg   Codice esadecimale stringa del colore di sfondo.
     */
    public static void saveThemeToDatabase(String hexPrim, String hexSec, String hexTer, String hexBg) {
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
                System.err.println("[ISCAT-DB] Errore salvataggio colori: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiorna lo stato di attivazione della Rainbow Mode nel modello, riproduce gli effetti
     * sonori di feedback appropriati e sincronizza in via asincrona la preferenza dell'utente nel database.
     *
     * @param model  Il modello di stato reattivo del tema da aggiornare.
     * @param active {@code true} per abilitare la Rainbow Mode, {@code false} per disabilitarla.
     */
    public static void setRainbowMode(ThemeSettingsModel model, boolean active) {
        model.rainbowModeProperty().set(active);
        if (active) {
            AudioManager.getInstance().playSFX("rainbow");
        } else {
            AudioManager.getInstance().playSFX("laugh");
            model.syncWithThemeManager();
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
                    System.err.println("[ISCAT-DB] Errore salvataggio flag rainbow: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Esegue il ripristino centralizzato dei parametri di configurazione visiva ai valori di fabbrica.
     * Resetta le proprietà in memoria del modello, azzera le strutture dati del carosello e
     * aggiorna in modo asincrono i record dell'utente all'interno del Database SQL.
     *
     * @param model Il modello di stato reattivo del tema su cui applicare il ripristino dei valori predefiniti.
     */
    public static void restoreDefaultTheme(ThemeSettingsModel model) {
        model.stopRainbowSyncTimer();
        model.isUpdatingProgrammatically = true;

        model.lightModeProperty().set(false);
        model.rainbowModeProperty().set(false);
        model.getCarouselImages().clear();
        model.getWritablePalette().clear();
        model.currentImageIndexProperty().set(-1);

        Color defPrimary = Color.web("#cbcbcb");
        Color defSecondary = Color.web("#a9a9a9");
        Color defTertiary = Color.web("#333333");
        Color defBg = Color.web("#010203");

        model.accentPrimaryProperty().set(defPrimary);
        model.accentSecondaryProperty().set(defSecondary);
        model.accentTernaryProperty().set(defTertiary);
        model.bgPrimaryProperty().set(defBg);

        model.isUpdatingProgrammatically = false;

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            settings.setLightmode(0);
            settings.setRainbowMode(0);
            settings.setPrimaryTheme(PaletteExtractor.toHex(defPrimary));
            settings.setSecondaryTheme(PaletteExtractor.toHex(defSecondary));
            settings.setTertiaryTheme(PaletteExtractor.toHex(defTertiary));
            settings.setBackgroundTheme(PaletteExtractor.toHex(defBg));

            IscatDB.getInstance().executeAsync(() -> {
                try {
                    SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                    dao.updateThemeSetting(settings.getUserId(), "Lightmode", "0");
                    dao.updateThemeSetting(settings.getUserId(), "RainbowMode", "0");
                    dao.updateThemeSetting(settings.getUserId(), "PrimaryTheme", PaletteExtractor.toHex(defPrimary));
                    dao.updateThemeSetting(settings.getUserId(), "SecondaryTheme", PaletteExtractor.toHex(defSecondary));
                    dao.updateThemeSetting(settings.getUserId(), "TertiaryTheme", PaletteExtractor.toHex(defTertiary));
                    dao.updateThemeSetting(settings.getUserId(), "BackgroundTheme", PaletteExtractor.toHex(defBg));
                } catch (Exception e) {
                    System.err.println("[ISCAT-DB] Errore durante il restore dei dati: " + e.getMessage());
                }
            });
        }
    }
}