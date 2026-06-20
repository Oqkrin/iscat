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

public class ThemeSettingsModel {

    private final ObjectProperty<Color> accentPrimary   = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> accentSecondary = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> accentTernary   = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> bgPrimary       = new SimpleObjectProperty<>(Color.BLACK);

    private final BooleanProperty lightMode   = new SimpleBooleanProperty(false);
    private final BooleanProperty rainbowMode = new SimpleBooleanProperty(false);

    private final List<Color> currentPalette = new ArrayList<>();
    private final List<File> carouselImages = new ArrayList<>();
    private final IntegerProperty currentImageIndex = new SimpleIntegerProperty(-1);

    private AnimationTimer rainbowUiSyncTimer;
    private boolean isUpdatingProgrammatically = false;   // <-- flag

    public ThemeSettingsModel() {
        accentPrimary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentSecondary.addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        accentTernary  .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
        bgPrimary      .addListener((obs, old, val) -> { if (!isUpdatingProgrammatically) applyThemeToManager(); });
    }

    // ==================== Properties ====================
    public ObjectProperty<Color> accentPrimaryProperty()   { return accentPrimary; }
    public ObjectProperty<Color> accentSecondaryProperty() { return accentSecondary; }
    public ObjectProperty<Color> accentTernaryProperty()   { return accentTernary; }
    public ObjectProperty<Color> bgPrimaryProperty()       { return bgPrimary; }

    public BooleanProperty lightModeProperty()   { return lightMode; }
    public BooleanProperty rainbowModeProperty() { return rainbowMode; }

    public IntegerProperty currentImageIndexProperty() { return currentImageIndex; }
    public List<Color> getCurrentPalette() { return Collections.unmodifiableList(currentPalette); }

    // ==================== Database loading ====================
    public void loadFromDatabase() {
        SessionManager session = SessionManager.getInstance();
        UserSettings settings = session.getCurrentSettings();
        if (settings == null) return;

        boolean dbLight   = settings.getLightmode() == 1;
        boolean dbRainbow = settings.getRainbowMode() == 1;

        lightMode.set(dbLight);
        rainbowMode.set(dbRainbow);

        isUpdatingProgrammatically = true;   // <-- prevent applyThemeToManager

        if (settings.getPrimaryTheme() != null && !"#FFFFFF".equalsIgnoreCase(settings.getPrimaryTheme())) {
            accentPrimary.set(Color.web(settings.getPrimaryTheme()));
            accentSecondary.set(Color.web(settings.getSecondaryTheme()));
            accentTernary.set(Color.web(settings.getTertiaryTheme()));
            bgPrimary.set(Color.web(settings.getBackgroundTheme()));
        } else {
            syncWithThemeManager();
        }

        isUpdatingProgrammatically = false;
    }

    public void syncWithThemeManager() {
        ThemeManager tm = ThemeManager.getInstance();
        isUpdatingProgrammatically = true;
        accentPrimary.set(tm.getAccentPrimary());
        accentSecondary.set(tm.getAccentSecondary());
        accentTernary.set(tm.getAccentTernary());
        bgPrimary.set(tm.getBgPrimary());
        isUpdatingProgrammatically = false;
    }

    // ==================== Manual colour change → save & stop rainbow ====================
    public void applyThemeToManager() {
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

    // ==================== Rainbow mode ====================
    public void setRainbowMode(boolean active) {
        rainbowMode.set(active);
        if (active) {
            AudioManager.getInstance().playSFX("rainbow");
        } else {
            AudioManager.getInstance().playSFX("laugh");
            // reapply the static theme when leaving rainbow
            syncWithThemeManager();
        }

        // persist the rainbow mode flag
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

    // ==================== Light mode ====================
    public void setLightMode(boolean light) {
        lightMode.set(light);
        // persist light mode flag
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

        // adjust colours (triggers listener → applyThemeToManager, stopping rainbow)
        if (!carouselImages.isEmpty()) {
            extractAndApplyPalette(carouselImages.get(currentImageIndex.get()));
        } else {
            Color cp = accentPrimary.get();
            bgPrimary.set(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, light ? 0.95 : 0.05));
            // the listener will call applyThemeToManager automatically
        }
    }

    // ==================== Image palette ====================
    public void addImageAndApply(File imageFile) {
        carouselImages.add(imageFile);
        currentImageIndex.set(carouselImages.size() - 1);
        extractAndApplyPalette(imageFile);
    }

    public void navigateCarousel(boolean next) {
        if (carouselImages.isEmpty()) return;
        int idx = currentImageIndex.get();
        int newIdx = next ? (idx + 1) % carouselImages.size()
                : (idx - 1 + carouselImages.size()) % carouselImages.size();
        currentImageIndex.set(newIdx);
        extractAndApplyPalette(carouselImages.get(newIdx));
    }

    private void extractAndApplyPalette(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            ThemeManager.getInstance().stopRainbowMode();
            stopRainbowSyncTimer();
            rainbowMode.set(false);

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, (int) IscatSettings.STANDARD_UNIT, 1, false);
            currentPalette.clear();
            for (int[] rgb : rawPalette) {
                currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }
            assignColorsFromPalette();   // this changes properties → listeners will call applyThemeToManager
        } catch (IOException e) {
            System.err.println("[ISCAT] Errore lettura immagine: " + e.getMessage());
        }
    }

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

    // ==================== Rainbow UI sync timer ====================
    public void startRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) rainbowUiSyncTimer.stop();
        rainbowUiSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                isUpdatingProgrammatically = true;   // <-- prevent saving
                Color c = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.set(c);
                accentSecondary.set(c);
                accentTernary.set(c);
                isUpdatingProgrammatically = false;
            }
        };
        rainbowUiSyncTimer.start();
    }

    public void stopRainbowSyncTimer() {
        if (rainbowUiSyncTimer != null) {
            rainbowUiSyncTimer.stop();
            rainbowUiSyncTimer = null;
        }
    }

    // ==================== Helpers ====================
    public static double luminance(Color c) {
        return 0.2126 * lin(c.getRed()) + 0.7152 * lin(c.getGreen()) + 0.0722 * lin(c.getBlue());
    }
    private static double lin(double ch) {
        return ch <= 0.03928 ? ch / 12.92 : Math.pow((ch + 0.055) / 1.055, 2.4);
    }
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
    public int getCarouselSize() { return carouselImages.size(); }
    public File getCurrentImage() {
        int idx = currentImageIndex.get();
        return (idx >= 0 && idx < carouselImages.size()) ? carouselImages.get(idx) : null;
    }
}