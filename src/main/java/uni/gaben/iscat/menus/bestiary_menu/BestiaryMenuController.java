package uni.gaben.iscat.menus.bestiary_menu;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.game.universe.enemies.iscat_eater.IscatEaterSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_mob.IscatMobSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_body_part.IscatWormBodyPartSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_head.IscatWormHeadSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_tail.IscatWormTailSettings;

import java.io.InputStream;
import java.util.Map;

public class BestiaryMenuController implements IscatFxmlController {

    /**
     * @param name       nome visualizzato
     * @param sprite     path della spritesheet (tutti i frame affiancati in orizzontale)
     * @param frameCount numero di frame (1 = immagine statica)
     * @param frameW     larghezza di UN singolo frame in pixel nativi
     * @param frameH     altezza di UN singolo frame in pixel nativi
     */
    private record Enemy(String name, String sprite, int frameCount, int frameW, int frameH, String description) {}

    private static final String BASE = "/uni/gaben/iscat/sprites/enemies/";
    private static final double DISPLAY_SIZE    = 160.0;   // dimensione preview sullo schermo
    private static final double FRAME_DURATION_MS = 150.0; // ms per frame

    private static final Map<String, Enemy> ENEMIES = Map.ofEntries(
            Map.entry("iscat_mob", new Enemy("Iscat Mob", BASE + "iscat.png",
                    IscatMobSettings.NUMERO_FRAMES, IscatMobSettings.DIM_SPRITE, IscatMobSettings.DIM_SPRITE,
                    "Descrizione Iscat\nFiglio di Iscat Mother, ama mangiare pankakes a colazione e pizza per pranzo e cena. ASPETTA QUESTA E' LA DESCRIZIONE! SORRY!! Iscat è un nemico che naviga per lo spazio in cerca di cibo solitamente in gruppo. Ha poca vita e poca potenza d'attacco.")),

            Map.entry("iscat_bomber", new Enemy("Iscat Bomber", BASE + "iscatBomber.png",
                    1, 32, 32,
                    "Descrizione Iscat Bomber")),

            Map.entry("iscat_core", new Enemy("Iscat Core", BASE + "iscat_core.png",
                    1, 64, 64,
                    "Descrizione Iscat Core")),

            Map.entry("iscat_mother", new Enemy("Iscat Mother", BASE + "iscat_mother.png",
                    2, 128, 128,
                    "Descrizione Iscat Mother")),

            Map.entry("fake_iscat", new Enemy("Fake Iscat", BASE + "fake_iscat.png",
                    19, 32, 32,
                    "Descrizione Fake Iscat")),

            Map.entry("fallen_star_golem", new Enemy("Fallen Star Golem", BASE + "fallen_star_golem.png",
                    25, 64, 64,
                    "Descrizione Fallen Star Golem")),

            Map.entry("eater", new Enemy("Eater", BASE + "eater.png",
                    IscatEaterSettings.NUMERO_FRAMES, IscatEaterSettings.DIM_SPRITE, IscatEaterSettings.DIM_SPRITE,
                    "Descrizione Eater")),

            Map.entry("iscat_worm_head", new Enemy("Iscat Worm Head", BASE + "iscat_worm_head.png",
                    IscatWormHeadSettings.NUMERO_FRAMES, IscatWormHeadSettings.DIM_SPRITE, IscatWormHeadSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Head")),

            Map.entry("iscat_worm_body_part", new Enemy("Iscat Worm Body Part", BASE + "iscat_worm_body_part.png",
                    IscatWormBodyPartSettings.NUMERO_FRAMES, IscatWormBodyPartSettings.DIM_SPRITE, IscatWormBodyPartSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Body Part")),

            Map.entry("iscat_worm_tail", new Enemy("Iscat Worm Tail", BASE + "iscat_worm_tail.png",
                    IscatWormTailSettings.NUMERO_FRAMES, IscatWormTailSettings.DIM_SPRITE, IscatWormTailSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Tail"))
    );

    // -------------------------------------------------------------------------
    // FXML
    // -------------------------------------------------------------------------

    @FXML private BorderPane rootPane;
    @FXML private ImageView  skinPreview;
    @FXML private Label      skinNameLabel;
    @FXML private TextArea description;

    private StackPane contentRoot;

    // -------------------------------------------------------------------------
    // Stato corrente
    // -------------------------------------------------------------------------

    private Timeline animation;
    private int      frameIndex = 0;

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        skinPreview.setSmooth(false);
        skinPreview.setPreserveRatio(true);
        skinPreview.setFitWidth(DISPLAY_SIZE);
        skinPreview.setFitHeight(DISPLAY_SIZE);
        description.setEditable(false);
        description.setWrapText(true);
        showEnemyById("iscat_mob");
    }

    // -------------------------------------------------------------------------
    // Handler — unico per tutti i bottoni
    // -------------------------------------------------------------------------

    @FXML
    private void showEnemy(ActionEvent event) {
        if (event.getSource() instanceof Button btn) {
            showEnemyById(btn.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Logica visualizzazione
    // -------------------------------------------------------------------------

    private void showEnemyById(String id) {
        Enemy enemy = ENEMIES.get(id);
        if (enemy == null) return;

        Image sheet = loadSheet(enemy.sprite());
        if (sheet == null) return; // sprite mancante: non crashare

        stopAnimation();
        skinNameLabel.setText(enemy.name().toUpperCase());
        description.setText(enemy.description());
        skinPreview.setImage(sheet);
        skinPreview.setViewport(viewport(enemy, 0)); // mostra frame 0

        if (enemy.frameCount() > 1) {
            startAnimation(enemy);
        }
    }

    // -------------------------------------------------------------------------
    // Animazione viewport
    // -------------------------------------------------------------------------

    private void startAnimation(Enemy enemy) {
        frameIndex = 0;
        animation = new Timeline(new KeyFrame(
                Duration.millis(FRAME_DURATION_MS),
                e -> {
                    frameIndex = (frameIndex + 1) % enemy.frameCount();
                    skinPreview.setViewport(viewport(enemy, frameIndex));
                }
        ));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    private void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
        frameIndex = 0;
    }

    /** Ritaglia il frame N dalla spritesheet orizzontale. */
    private static Rectangle2D viewport(Enemy enemy, int frame) {
        return new Rectangle2D(
                (double) frame * enemy.frameW(), 0,
                enemy.frameW(), enemy.frameH()
        );
    }

    // -------------------------------------------------------------------------
    // Caricamento immagine — sicuro (no NPE)
    // -------------------------------------------------------------------------

    private Image loadSheet(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            System.err.println("[Bestiary] Sprite non trovato: " + path);
            return null;
        }
        // Carichiamo a dimensione nativa — il viewport + fitWidth/fitHeight pensano allo scaling
        return new Image(is);
    }

    // -------------------------------------------------------------------------
    // Navigazione
    // -------------------------------------------------------------------------

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (contentRoot != null) {
            IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
        } else {
            IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
        }
    }
}