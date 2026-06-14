package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.List;

public class SkinMenuController implements IscatMenuController {

    private enum InfoMode {
        DESCRIPTION, STATS, EXTRA
    }

    private InfoMode currentInfoMode = InfoMode.DESCRIPTION;

    @FXML private GridPane skinGrid;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;

    @FXML private Button confirmBtn;
    @FXML private Button randomBtn;
    @FXML private Button cancelBtn;

    @FXML private Button btnDescription;
    @FXML private Button btnStats;
    @FXML private Button btnExtra;

    @FXML private InfoCardController infoCardController;

    private StackPane contentRoot;
    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    private static final int TOTAL_SKINS = 9;
    private static final double BASE_SIZE = 32.0;

    private boolean isScaling = false;

    private static final String[] SKIN_NAMES = {
            "Battle Ship", "Simple Battle Ship", "Friendly Ship",
            "Iscat Traitor", "Cubism Lover", "Space Goblin",
            "Phantom", "Radar", "PARADOX"
    };

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(BASE_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(randomBtn,  "fas-dice");
        ComponentsUtils.applyIconButton(cancelBtn,  "fas-arrow-left");

        // Iconizzazione opzionale dei bottoni info con FontAwesome
        ComponentsUtils.applyIconButton(btnDescription, "fas-book");
        ComponentsUtils.applyIconButton(btnStats,       "fas-chart-bar");
        ComponentsUtils.applyIconButton(btnExtra,       "fas-info-circle");

        populateGrid();

        // Stato iniziale basato su player1
        this.selectedSkinKey = "player1";
        this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
        this.skinNameLabel.setText(SKIN_NAMES[0].toUpperCase());
        previewCanvas.loadSkin(selectedSkinPath);

        // Forza la sincronizzazione iniziale sulla info card
        refreshInfoZone();

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();
        skinStackPane.widthProperty().addListener(sizeListener);
        skinStackPane.heightProperty().addListener(sizeListener);

        Platform.runLater(this::updateDynamicScaling);

        registerEscHandler();
    }

    private void updateDynamicScaling() {
        if (isScaling) return;
        double w = skinStackPane.getWidth();
        double h = skinStackPane.getHeight();
        if (w < 200 || h < 200) return;

        isScaling = true;

        double iconDim = getIconDim(w, h);
        double previewDim = iconDim * 3.5;
        double btnDim = iconDim + 30;

        for (Node node : skinGrid.getChildren()) {
            if (node instanceof Button btn && btn.getGraphic() instanceof AnimatedCanvas canvas) {
                btn.setMinSize(btnDim, btnDim);
                btn.setPrefSize(btnDim, btnDim);
                btn.setMaxSize(btnDim, btnDim);
                canvas.resize(iconDim);
            }
        }

        double fattoreScala = 1.25;

        skinGrid.setScaleX(fattoreScala);
        skinGrid.setScaleY(fattoreScala);

        previewCanvas.resize(previewDim);
        previewBox.setMinSize(previewDim + 100, previewDim + 150);
        previewBox.setPrefWidth(previewDim + 100);

        Platform.runLater(() -> isScaling = false);
    }

    private static double getIconDim(double w, double h) {
        double multiplier = Math.max(1.0, Math.min(w, h) / 400.0);
        multiplier = Math.round(multiplier * 5.0) / 5.0;
        return BASE_SIZE * multiplier;
    }

    private void populateGrid() {
        skinGrid.getChildren().clear();
        buttonCanvases.clear();
        double initialDim = 80.0;

        for (int i = 0; i < TOTAL_SKINS; i++) {
            int num = i + 1;
            String key = "player" + num;
            String path = "/uni/gaben/iscat/sprites/players/player" + num + ".png";
            String name = SKIN_NAMES[i];

            AnimatedCanvas canvas = new AnimatedCanvas(BASE_SIZE);
            canvas.loadSkin(path);
            buttonCanvases.add(canvas);

            Button btn = new Button();
            btn.getStyleClass().add("skin-button");
            btn.setGraphic(canvas);
            btn.setFocusTraversable(false);

            btn.setMinSize(initialDim, initialDim);
            btn.setPrefSize(initialDim, initialDim);
            btn.setMaxSize(initialDim, initialDim);

            btn.setOnAction(e -> selectSkin(key, path, name));

            skinGrid.add(btn, i % 3, i / 3);
        }
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey = key;
        this.selectedSkinPath = path;
        this.skinNameLabel.setText(name.toUpperCase());
        previewCanvas.loadSkin(path);

        // Sincronizza i dati testuali sulla InfoCard a destra
        refreshInfoZone();

        ComponentsUtils.playSpawnTween(previewBox);
        updateDynamicScaling();
    }

    /**
     * Recupera le statistiche dell'entità corrente dalla cache
     * e aggiorna i testi della InfoCard aggregata.
     */
    private void refreshInfoZone() {
        if (selectedSkinKey == null || infoCardController == null) return;

        EntityRecord record = EntityFactory.getCache().get(selectedSkinKey);
        if (record == null) {
            // Se la factory non ha ancora in cache il JSON del player, mostra un fallback amichevole
            infoCardController.updateInfo("N/A", "Nessun dato JSON caricato per " + selectedSkinKey);
            return;
        }

        switch (currentInfoMode) {
            case DESCRIPTION -> {
                infoCardController.updateInfo("DESCRIPTION", record.description());
            }
            case STATS -> {
                String statsText = String.format("""
                    STATISTICHE DELLA NAVE
                    
                    ❤ Integrità Scafo: %.0f HP
                    ⚡ Velocità di Manovra: %.1f m/s
                    📐 Scala Grafica: %.1fx
                    ⚓ Coefficiente Attrito: %.1f
                    ⚙ Massa Strutturale: %.1f kg
                    💪 Spinta Propulsori: %.1f N
                    """,
                        record.initLife(), record.maxVelocity(), record.scale(),
                        record.linearDamping(), record.mass(), record.maxForce()
                );
                infoCardController.updateInfo("STATS", statsText);
            }
            case EXTRA -> {
                double cooldownSparo = record.actionCooldownSec();
                double dashImpulse = 0;
                double dashCooldown = 0;

                // Estrazione sicura del sotto-record specifico del player
                if (record.player() != null) {
                    dashImpulse = record.player().dashImpulse();
                    dashCooldown = record.player().dashCooldownSec();
                    if (record.player().baseCooldownSec() > 0) {
                        cooldownSparo = record.player().baseCooldownSec();
                    }
                }

                String extraText = String.format("""
                    SPECIFICHE DI SISTEMA
                    
                    ⏱ Cooldown Fuoco Base: %.2f sec
                    💨 Impulso Propulsione (Dash): %.1f N/s
                    ⏱ Ricarica Scatto (Dash): %.2f sec
                    🆔 ID Interno Risorsa: %s
                    """,
                        cooldownSparo, dashImpulse, dashCooldown, record.entityKey()
                );
                infoCardController.updateInfo("EXTRA INFO", extraText);
            }
        }
    }

    @FXML private void showDescription() { currentInfoMode = InfoMode.DESCRIPTION; refreshInfoZone(); }
    @FXML private void showStats()       { currentInfoMode = InfoMode.STATS; refreshInfoZone(); }
    @FXML private void showExtra()       { currentInfoMode = InfoMode.EXTRA; refreshInfoZone(); }

    @FXML
    private void selectRandom() {
        int idx;
        String key;
        String path;
        do {
            idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(TOTAL_SKINS);
            key = "player" + (idx + 1);
            path = "/uni/gaben/iscat/sprites/players/player" + (idx + 1) + ".png";
        } while (path.equals(selectedSkinPath));

        selectSkin(key, path, SKIN_NAMES[idx]);
    }

    @Override
    public Pane getRootPane() { return skinStackPane; }

    @FXML
    private void handleBack(ActionEvent event) { handleBack(); }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null && selectedSkinKey != null) {
            PlayerSettings.setPlayerSkin(selectedSkinPath);
            PlayerSettings.setPlayerSkinKey(selectedSkinKey);

            try {
                System.out.println("[SkinMenu] Salvata skin nel Database: " + selectedSkinKey);
            } catch (Exception ex) {
                System.err.println("[SkinMenu] Impossibile salvare la skin nel DB: " + ex.getMessage());
            }
        }
        handleBack();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}