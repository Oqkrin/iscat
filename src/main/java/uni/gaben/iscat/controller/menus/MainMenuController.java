package uni.gaben.iscat.controller.menus;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.List;
import java.util.Map;

/**
 * Controller per il menu principale. Gestisce la navigazione, il layout dinamico dei
 * pulsanti e l'inizializzazione asincrona delle icone animate e delle skin dei player.
 */
public class MainMenuController implements IscatFxmlController {

    @FXML private Button playButton;
    @FXML private Button tutorialButton;
    @FXML private Button settingsButton;
    @FXML private Button scoreButton;
    @FXML private Button skinButton;
    @FXML private Button bestiaryButton;
    @FXML private Button entityEditorButton;
    @FXML private Button logoutButton;
    @FXML private Button quitButton;
    @FXML private Button leaderboardButton;
    @FXML private Button creditsButton;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    private StackPane contentRoot;
    private final DoubleProperty sideButtonsMaxSide = new SimpleDoubleProperty(0);

    private AnimatedCanvas currentSkinCanvas;
    private boolean skinListenerAdded = false;

    /**
     * Inizializza i pulsanti del menu, sincronizza le dimensioni dei moduli quadrati
     * laterali, registra i listener per i cambi di skin e istanzia la veste grafica iniziale.
     */
    @FXML
    public void initialize() {
        List<Button> sideButtons = List.of(leaderboardButton, scoreButton, bestiaryButton,
                entityEditorButton, skinButton);

        for (Button btn : sideButtons) {
            btn.widthProperty().addListener((obs, oldVal, newVal) -> updateMaxSide(newVal.doubleValue()));
            btn.heightProperty().addListener((obs, oldVal, newVal) -> updateMaxSide(newVal.doubleValue()));
        }

        sideButtonsMaxSide.addListener((obs, oldVal, newVal) -> {
            double size = newVal.doubleValue();
            for (Button btn : sideButtons) {
                btn.setMinWidth(size);
                btn.setMaxWidth(size);
                btn.setMinHeight(size);
                btn.setMaxHeight(size);
            }
        });

        if (!skinListenerAdded) {
            SessionManager.playerSkinProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> refreshSkin(skinButton));
            });
            skinListenerAdded = true;
        }

        setIcon(playButton,        "fas-rocket");
        setIcon(tutorialButton,    "fas-graduation-cap");
        setIcon(settingsButton,    "fas-cog");
        setIcon(scoreButton,       "fas-eye");
        setIcon(skinButton,        "fas-gift");
        setIcon(bestiaryButton,    "fas-bug");
        setIcon(entityEditorButton,"fas-edit");
        setIcon(logoutButton,      "fas-sign-out-alt");
        setIcon(quitButton,        "fas-door-open");
        setIcon(leaderboardButton, "fas-list-ol");
        setIcon(creditsButton,     "fab-creative-commons");

        refreshSkin(skinButton);
    }

    private void updateMaxSide(double value) {
        if (value > sideButtonsMaxSide.get()) {
            sideButtonsMaxSide.set(value);
        }
    }

    @FXML public void playGame()            { navigate(IscatViews.GAME);             }
    @FXML public void openTutorialMenu()    { navigate(IscatViews.TUTORIAL_MENU);    }
    @FXML public void openSettingsMenu()    { navigate(IscatViews.SETTINGS_MENU);    }
    @FXML public void openScoreMenu()       { navigate(IscatViews.SCORE_MENU);       }
    @FXML public void openSkinMenu()        { navigate(IscatViews.SKIN_MENU);        }
    @FXML public void openBestiaryMenu()    { navigate(IscatViews.BESTIARY_MENU);    }

    @FXML public void openEntityEditor()    {
        EntityEditorMenuController.targetEntityKeyToLoad = null;
        navigate(IscatViews.ENTITY_EDITOR);
    }

    @FXML public void logout()              { navigate(IscatViews.LOGIN_MENU);       }
    @FXML public void openLeaderboardMenu() { navigate(IscatViews.LEADERBOARD_MENU); }
    @FXML public void openCreditsMenu()     { navigate(IscatViews.CREDITS);          }

    /**
     * Gestisce la chiusura in sicurezza del gioco tramite overlay di conferma.
     */
    @FXML
    public void quit() {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Uscire dal gioco?",
                    "Sei sicuro di voler chiudere ISCAT?",
                    Platform::exit
            );
        } else {
            Platform.exit();
        }
    }

    private void navigate(IscatViews scene) {
        IscatNavigator.getInstance().navigateWithFade(scene);
    }

    /**
     * Associa l'elemento iconico statico o l'animazione grafica corretta al relativo bottone.
     *
     * @param btn      Il bottone di destinazione.
     * @param iconCode Il codice identificativo o di stile dell'icona richiesta.
     */
    private void setIcon(Button btn, String iconCode) {
        if (btn == null) return;

        try {
            switch (iconCode) {
                case "fas-gift" -> {
                }
                case "fas-bug" -> {
                    AnimatedCanvas canvas = new AnimatedCanvas(128);
                    loadAndResizeSkin(canvas, "/uni/gaben/iscat/sprites/enemies/iscat_mob.png", 32, 32);
                    canvas.setFrameDuration(0.20);
                    btn.setGraphic(canvas);
                }
                case "fas-edit" -> {
                    AnimatedCanvas canvas = new AnimatedCanvas(128);
                    loadAndResizeSkin(canvas, "/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
                    canvas.setFrameDuration(0.20);
                    btn.setGraphic(canvas);
                }
                case "fas-eye", "fas-list-ol" -> {
                    FontIcon icon = new FontIcon(iconCode);
                    icon.setIconSize(128);
                    btn.setGraphic(icon);
                }
                default -> {
                    FontIcon icon = new FontIcon(iconCode);
                    icon.setIconSize(32);
                    btn.setGraphic(icon);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'icona: " + iconCode + " per il bottone " + btn.getId());
            e.printStackTrace();
        }
    }

    /**
     * Ferma l'animazione in esecuzione e ricostruisce in modo proporzionale la canvas
     * della skin in base alle dimensioni reali estratte dal record.
     *
     * @param btn Il bottone grafico ospitante la skin aggiornata.
     */
    private void refreshSkin(Button btn) {
        if (btn == null) return;

        String skinKey = SessionManager.getPlayerSkinKey();
        Map<String, EntityRecord> cache = EntityFactory.getCache();
        EntityRecord record = cache.get(skinKey.toLowerCase().trim());

        if (record == null) {
            System.err.println("[MainMenu] Record skin non trovato: " + skinKey);
            return;
        }

        if (currentSkinCanvas != null) {
            currentSkinCanvas.stop();
        }

        AnimatedCanvas newCanvas = new AnimatedCanvas(128);
        newCanvas.loadSkin(record.spritePath(), record.frameW(), record.frameH());

        double maxSize = 128.0;
        double scale = (record.frameW() > record.frameH())
                ? maxSize / record.frameW()
                : maxSize / record.frameH();
        newCanvas.setWidth(record.frameW() * scale);
        newCanvas.setHeight(record.frameH() * scale);
        newCanvas.setFrameDuration(0.20);

        currentSkinCanvas = newCanvas;
        btn.setGraphic(newCanvas);
    }

    /**
     * Carica il foglio sprite all'interno della canvas e ne riproporziona i vincoli hardware
     * calcolandone il fattore di scala sul lato massimo.
     *
     * @param canvas Il contenitore grafico animato da manipolare.
     * @param path   La stringa del percorso della risorsa visiva.
     * @param frameW Larghezza nativa del singolo frame.
     * @param frameH Altezza nativa del singolo frame.
     */
    private void loadAndResizeSkin(AnimatedCanvas canvas, String path, int frameW, int frameH) {
        canvas.loadSkin(path, frameW, frameH);
        double maxSize = 128.0;
        double scale = (frameW > frameH) ? maxSize / frameW : maxSize / frameH;
        canvas.setWidth(frameW * scale);
        canvas.setHeight(frameH * scale);
    }

    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }
}