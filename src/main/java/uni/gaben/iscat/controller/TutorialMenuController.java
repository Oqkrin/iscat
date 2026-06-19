package uni.gaben.iscat.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller per la schermata dei comandi e del tutorial di gioco (Iscat Tutorial).
 * Gestisce l'interfaccia utente del menu e implementa un effetto scenico a sistema
 * solare in primo piano, inserendo un layer trasparente nello StackPane globale dove
 * un insieme di skin di gioco casuali orbitano attorno al contenitore centrale.
 *
 * @author Gemini
 * @version 1.0
 */
public class TutorialMenuController implements IscatMenuController {

    /** Il pannello radice definito nell'FXML che organizza i macro-componenti del menu. */
    @FXML private BorderPane rootPane;

    /** Il pulsante adibito al ritorno verso il menu principale. */
    @FXML private Button backBtn;

    /** Il riferimento allo StackPane radice della vista corrente, utilizzato come destinazione per i layer sovrapposti. */
    private StackPane contentRoot;

    /** Il timer ad alta precisione responsabile dell'aggiornamento frame-by-frame della posizione dei pianeti orbitanti. */
    private AnimationTimer solarSystemTimer;

    /** Il pannello trasparente superiore iniettato dinamicamente per contenere ed isolare i bottoni delle skin orbitanti. */
    private Pane spaceOverlay;

    /** La lista contenente i nodi logici e grafici di ciascun pianeta inserito nel sistema solare. */
    private final List<PlanetNode> planets = new ArrayList<>();

    /** Il numero massimo di elementi (skin di gioco) che devono orbitare simultaneamente nel sistema. */
    private static final int NUMBER_OF_PLANETS = 6;

    /** Il raggio dell'orbita circolare (espresso in pixel) calcolato a partire dal centro geometrico di riferimento. */
    private static final double ORBIT_RADIUS = 310.0;

    /** La velocità angolare di base espressa in radianti per frame utilizzata per calcolare lo spostamento dei pianeti. */
    private static final double BASE_SPEED = 0.012;

    /**
     * Struttura dati interna adibita al monitoraggio dello stato geometrico,
     * cinematico e grafico di un singolo elemento orbitante nel sistema solare.
     */
    private static class PlanetNode {
        /** Il componente grafico Button associato al pianeta e contenente la skin animata. */
        Button button;
        /** L'angolo di rotazione corrente espresso in radianti. */
        double angle;
        /** Il coefficiente moltiplicativo casuale applicato alla velocità di base per diversificare il moto. */
        double speedModifier;
    }

    /**
     * Inizializza i componenti grafici del controller.
     * Configura l'icona del pulsante di ritorno e pianifica l'inizializzazione
     * del sistema solare sul thread dell'applicazione JavaFX non appena il layout è pronto.
     */
    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(backBtn, "fas-arrow-left");
        Platform.runLater(this::setupSolarSystem);
    }

    /**
     * Configura, istanzia e avvia l'intero effetto scenico del sistema solare.
     * Estrae le skin dei giocatori registrate nella cache globale, seleziona un set casuale
     * di elementi, li ancora ad un pannello sovrapposto in primo piano assoluto e ne
     * attiva le orbite sincronizzate calcolandone il baricentro dinamico tramite un {@link AnimationTimer}.
     */
    private void setupSolarSystem() {
        List<EntityRecord> availableSkins = new ArrayList<>();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();

        if (globalCache != null) {
            globalCache.values().stream()
                    .filter(record -> record != null && (record.player() != null || record.entityKey().toLowerCase().contains("player")))
                    .forEach(availableSkins::add);
        }

        if (availableSkins.isEmpty()) return;

        Collections.shuffle(availableSkins);

        StackPane topStackPane = this.contentRoot;
        if (topStackPane == null && rootPane.getScene() != null && rootPane.getScene().getRoot() instanceof StackPane sp) {
            topStackPane = sp;
        }
        if (topStackPane == null) return;

        spaceOverlay = new Pane();
        spaceOverlay.setPickOnBounds(false);

        topStackPane.getChildren().add(spaceOverlay);
        spaceOverlay.toFront();

        VBox centerContainer = null;
        if (rootPane.getCenter() instanceof VBox vbox) {
            centerContainer = vbox;
        }

        final VBox referenceCenter = centerContainer;

        int count = Math.min(NUMBER_OF_PLANETS, availableSkins.size());
        double angleStep = (2 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            EntityRecord skinRecord = availableSkins.get(i);

            AnimatedCanvas canvas = new AnimatedCanvas(96.0);
            canvas.loadSkin(skinRecord.spritePath(), skinRecord.frameW(), skinRecord.frameH());
            canvas.resize(80.0);
            canvas.setFrameDuration(0.15);

            Button planetBtn = new Button();
            planetBtn.getStyleClass().add("skin-button");
            planetBtn.setGraphic(canvas);
            planetBtn.setFocusTraversable(false);

            planetBtn.setMinSize(96, 96);
            planetBtn.setMaxSize(96, 96);

            PlanetNode planet = new PlanetNode();
            planet.button = planetBtn;
            planet.angle = i * angleStep;
            planet.speedModifier = 0.85 + (Math.random() * 0.3);

            planets.add(planet);
            spaceOverlay.getChildren().add(planetBtn);
        }

        solarSystemTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double centerX = spaceOverlay.getWidth() / 2.0;
                double centerY = spaceOverlay.getHeight() / 2.0;

                if (referenceCenter != null && referenceCenter.getWidth() > 0) {
                    centerX = referenceCenter.getLocalToSceneTransform().getTx() + (referenceCenter.getWidth() / 2.0);
                    centerY = referenceCenter.getLocalToSceneTransform().getTy() + (referenceCenter.getHeight() / 2.0) - 40.0;
                }

                for (PlanetNode planet : planets) {
                    planet.angle += BASE_SPEED * planet.speedModifier;
                    if (planet.angle > 2 * Math.PI) {
                        planet.angle -= 2 * Math.PI;
                    }

                    double posX = centerX + Math.cos(planet.angle) * ORBIT_RADIUS - (planet.button.getWidth() / 2.0);
                    double posY = centerY + Math.sin(planet.angle) * ORBIT_RADIUS - (planet.button.getHeight() / 2.0);

                    planet.button.setLayoutX(posX);
                    planet.button.setLayoutY(posY);
                }
            }
        };
        solarSystemTimer.start();
    }

    /**
     * Gestisce l'evento di pressione sul pulsante di ritorno.
     * Interrompe in sicurezza il timer del sistema solare per evitare leak di memoria,
     * rimuove il pannello sovrapposto dei pianeti dallo StackPane principale ed effettua la navigazione a ritroso.
     *
     * @param event L'evento di azione generato dal click sul pulsante.
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        if (solarSystemTimer != null) {
            solarSystemTimer.stop();
        }
        if (spaceOverlay != null && spaceOverlay.getParent() instanceof StackPane parent) {
            parent.getChildren().remove(spaceOverlay);
        }
        handleBack();
    }

    /**
     * Imposta il puntatore allo StackPane del contenitore radice per scopi di navigazione.
     *
     * @param pointer Lo StackPane radice utilizzato dall'istanza del navigatore dell'applicazione.
     */
    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }

    /**
     * Restituisce il pannello radice associato a questa vista.
     *
     * @return Il {@link Pane} corrispondente all'elemento radice fxml (in questo caso un BorderPane).
     */
    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}