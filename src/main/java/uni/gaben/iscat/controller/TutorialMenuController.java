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
 * solare in primo piano.
 *
 */
public class TutorialMenuController implements IscatMenuController {

    @FXML private BorderPane rootPane;
    @FXML private Button backBtn;

    private StackPane contentRoot;
    private AnimationTimer solarSystemTimer;
    private Pane spaceOverlay;
    private final List<PlanetNode> planets = new ArrayList<>();

    private static final int NUMBER_OF_PLANETS = 6;
    private static final double ORBIT_RADIUS = 310.0;
    private static final double BASE_SPEED = 0.012;

    private static class PlanetNode {
        Button button;
        double angle;
        double speedModifier;
    }

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(backBtn, "fas-arrow-left");

        rootPane.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                Platform.runLater(this::setupSolarSystem);
            } else {
                clearSolarSystem();
            }
        });

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && rootPane.isVisible()) {
                Platform.runLater(this::setupSolarSystem);
            }
        });
    }

    private void clearSolarSystem() {
        if (solarSystemTimer != null) {
            solarSystemTimer.stop();
            solarSystemTimer = null;
        }
        if (spaceOverlay != null) {
            if (spaceOverlay.getParent() instanceof StackPane parent) {
                parent.getChildren().remove(spaceOverlay);
            }
            spaceOverlay.getChildren().clear();
            spaceOverlay = null;
        }
        planets.clear();
    }

    private void setupSolarSystem() {
        clearSolarSystem();

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
                if (spaceOverlay == null) return;

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

    @FXML
    private void handleBackAction(ActionEvent event) {
        if (solarSystemTimer != null) {
            solarSystemTimer.stop();
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