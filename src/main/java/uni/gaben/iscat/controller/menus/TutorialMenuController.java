package uni.gaben.iscat.controller.menus;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller per il menu del Tutorial.
 * Gestisce la visualizzazione dinamica dei comandi utente e un'animazione coreografica
 * ad anello in cui le navicelle dei player orbitano attorno al centro dello schermo.
 * Premendo sulle navicelle in orbita viene riprodotto un effetto sonoro casuale.
 */
public class TutorialMenuController implements IscatMenuController {

    @FXML private BorderPane rootPane;
    @FXML private Button backBtn;

    @FXML private Label moveLabel;
    @FXML private Label attackLabel;
    @FXML private Label dashLabel;

    private StackPane contentRoot;
    private AnimationTimer orbitTimer;
    private Pane overlayContainer;
    private final List<OrbitNode> orbitingNodes = new ArrayList<>();
    private UserSettings userSettings;

    private static final int NUMBER_OF_PLAYERS = 6;
    private static final double ORBIT_RADIUS = 310.0;
    private static final double BASE_SPEED = 0.012;

    /**
     * Struttura dati interna per tracciare la posizione e lo stato
     * di un elemento grafico in orbita circolare.
     */
    private static class OrbitNode {
        Button button;
        double angle;
        double speedModifier;
    }

    /**
     * Imposta le impostazioni utente correnti per configurare i tasti dei comandi.
     *
     * @param userSettings Le impostazioni dell'utente.
     */
    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * Inizializza il controller, configura i nodi grafici e registra i listener
     * per far partire o arrestare l'animazione dell'orbita in base alla visibilità della UI.
     */
    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(backBtn, "fas-arrow-left");

        rootPane.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                Platform.runLater(this::setupOrbitAnimation);
            } else {
                clearOrbitAnimation();
            }
        });

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && rootPane.isVisible()) {
                Platform.runLater(this::setupOrbitAnimation);
            }
        });
    }

    /**
     * Ferma il timer dell'animazione, svuota i contenitori grafici
     * e pulisce la lista dei nodi tracciati in orbita.
     */
    private void clearOrbitAnimation() {
        if (orbitTimer != null) {
            orbitTimer.stop();
            orbitTimer = null;
        }
        if (overlayContainer != null) {
            if (overlayContainer.getParent() instanceof StackPane parent) {
                parent.getChildren().remove(overlayContainer);
            }
            overlayContainer.getChildren().clear();
            overlayContainer = null;
        }
        orbitingNodes.clear();
    }

    /**
     * Configura da zero l'animazione dell'orbita circolare dei player.
     * Recupera le skin disponibili, istanzia i bottoni animati, applica gli eventi
     * per la riproduzione audio casuale e avvia l'{@link AnimationTimer}.
     */
    private void setupOrbitAnimation() {
        this.userSettings = SessionManager.getInstance().getCurrentSettings();
        updateStaticLabels();
        clearOrbitAnimation();

        List<EntityRecord> availableSkins = new ArrayList<>();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();

        globalCache.values().stream()
                .filter(record -> record != null && (record.player() != null || record.entityKey().toLowerCase().contains("player")))
                .forEach(availableSkins::add);

        if (availableSkins.isEmpty()) return;

        Collections.shuffle(availableSkins);

        StackPane topStackPane = this.contentRoot;
        if (topStackPane == null && rootPane.getScene() != null && rootPane.getScene().getRoot() instanceof StackPane sp) {
            topStackPane = sp;
        }
        if (topStackPane == null) return;

        overlayContainer = new Pane();
        overlayContainer.setPickOnBounds(false);

        topStackPane.getChildren().add(overlayContainer);
        overlayContainer.toFront();

        VBox centerContainer = null;
        if (rootPane.getCenter() instanceof VBox vbox) {
            centerContainer = vbox;
        }

        final VBox referenceCenter = centerContainer;

        int count = Math.min(NUMBER_OF_PLAYERS, availableSkins.size());
        double angleStep = (2 * Math.PI) / count;

        List<String> controlLabels = getControlLabels();

        for (int i = 0; i < count; i++) {
            EntityRecord skinRecord = availableSkins.get(i);

            AnimatedCanvas canvas = new AnimatedCanvas(96.0);
            canvas.loadSkin(skinRecord.spritePath(), skinRecord.frameW(), skinRecord.frameH());
            canvas.resize(80.0);
            canvas.setFrameDuration(0.15);

            Button playerBtn = new Button();
            playerBtn.getStyleClass().add("skin-button");
            playerBtn.setGraphic(canvas);
            playerBtn.setFocusTraversable(false);

            if (i < controlLabels.size()) {
                playerBtn.setText(controlLabels.get(i));
            }

            playerBtn.setMinSize(96, 96);
            playerBtn.setMaxSize(96, 96);

            playerBtn.setOnAction(e -> {
                String randomSfx = AudioManager.getInstance().getRandomSfxKey();
                if (randomSfx != null) {
                    AudioManager.getInstance().playSFX(randomSfx);
                }
            });

            OrbitNode node = new OrbitNode();
            node.button = playerBtn;
            node.angle = i * angleStep;
            node.speedModifier = 0.85 + (Math.random() * 0.3);

            orbitingNodes.add(node);
            overlayContainer.getChildren().add(playerBtn);
        }

        orbitTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (overlayContainer == null) return;

                double centerX = overlayContainer.getWidth() / 2.0;
                double centerY = overlayContainer.getHeight() / 2.0;

                if (referenceCenter != null && referenceCenter.getWidth() > 0) {
                    centerX = referenceCenter.getLocalToSceneTransform().getTx() + (referenceCenter.getWidth() / 2.0);
                    centerY = referenceCenter.getLocalToSceneTransform().getTy() + (referenceCenter.getHeight() / 2.0) - 40.0;
                }

                for (OrbitNode node : orbitingNodes) {
                    node.angle += BASE_SPEED * node.speedModifier;
                    if (node.angle > 2 * Math.PI) {
                        node.angle -= 2 * Math.PI;
                    }

                    double posX = centerX + Math.cos(node.angle) * ORBIT_RADIUS - (node.button.getWidth() / 2.0);
                    double posY = centerY + Math.sin(node.angle) * ORBIT_RADIUS - (node.button.getHeight() / 2.0);

                    node.button.setLayoutX(posX);
                    node.button.setLayoutY(posY);
                }
            }
        };
        orbitTimer.start();
    }

    /**
     * Aggiorna i testi delle etichette statiche della schermata mostrando i tasti attuali
     * di movimento, attacco e dash salvati nelle impostazioni utente.
     */
    private void updateStaticLabels() {
        if (userSettings == null || moveLabel == null || attackLabel == null || dashLabel == null) return;

        String keys = userSettings.getWalkUp() + userSettings.getWalkLeft() + userSettings.getWalkDown() + userSettings.getWalkRight();
        moveLabel.setText(keys.toUpperCase());
        attackLabel.setText(userSettings.getAttack());
        dashLabel.setText(userSettings.getDash1());
    }

    /**
     * Costruisce e restituisce una lista formattata di stringhe descrittive relative ai comandi di gioco.
     * Se le impostazioni utente non sono caricate, restituisce dei placeholder standard.
     *
     * @return Lista di stringhe contenenti le descrizioni dei controlli associati ai tasti.
     */
    private List<String> getControlLabels() {
        List<String> labels = new ArrayList<>();
        if (userSettings == null) {
            labels.add("SU");
            labels.add("GIÙ");
            labels.add("SINISTRA");
            labels.add("DESTRA");
            labels.add("ATTACCO");
            labels.add("DASH");
            return labels;
        }

        labels.add("Su: " + userSettings.getWalkUp());
        labels.add("Giù: " + userSettings.getWalkDown());
        labels.add("Sinistra: " + userSettings.getWalkLeft());
        labels.add("Destra: " + userSettings.getWalkRight());
        labels.add("Attacco: " + userSettings.getAttack());
        labels.add("Dash: " + userSettings.getDash1());
        return labels;
    }

    /**
     * Gestisce la pressione del pulsante indietro fermando in sicurezza il loop di animazione.
     *
     * @param event L'evento di azione generato dal click.
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        if (orbitTimer != null) {
            orbitTimer.stop();
        }
        handleBack();
    }

    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}