package uni.gaben.iscat.view.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.view.game.debug.DebugToolBar;
import java.util.function.Consumer;

/**
 * Componente grafico dedicato alla visualizzazione e gestione degli strumenti di debug a schermo,
 * inclusi i pulsanti di controllo rapido e le notifiche sui cheat attivi.
 */
public class GameDebugOverlay extends StackPane {

    private final GameController gameController;
    private final GameModel gameModel;

    private DebugToolBar debugToolBar;
    private HBox debugButtonsContainer;
    private Button debugButton;
    private Button toggleWave;

    private Label godModeLabel;
    private Label debugWarningLabel;
    private VBox cheatLabelsContainer;

    private boolean debugPanelVisible = false;

    /**
     * Costruisce l'overlay di debug agganciando i nodi visivi alle proprietà del controller.
     *
     * @param gameController      Il controller di gioco
     * @param onToggleDebugPanel Callback invocata quando il pannello cambia visibilità, passa lo stato corrente
     */
    public GameDebugOverlay(GameController gameController, Consumer<Boolean> onToggleDebugPanel) {
        this.gameController = gameController;
        this.gameModel = gameController.getGameModel();

        setPickOnBounds(false);
        initNodes();
        initStyles();
        initLayout();
        initBindings(onToggleDebugPanel);
        initEventHandlers(onToggleDebugPanel);
    }

    private void initNodes() {
        debugToolBar = new DebugToolBar(gameController);
        debugToolBar.setPickOnBounds(false);

        godModeLabel = new Label("GOD MODE ACTIVE");
        godModeLabel.setFocusTraversable(false);
        godModeLabel.setMouseTransparent(true);

        debugWarningLabel = new Label("DEBUG MODE ACTIVATED - SCORE WILL NOT BE SAVED");
        debugWarningLabel.setFocusTraversable(false);
        debugWarningLabel.setMouseTransparent(true);

        cheatLabelsContainer = new VBox(12, debugWarningLabel, godModeLabel);
        cheatLabelsContainer.setFocusTraversable(false);
        cheatLabelsContainer.setMouseTransparent(true);

        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        toggleWave = new Button("pause wave");
        toggleWave.setFocusTraversable(false);

        debugButtonsContainer = new HBox(10, debugButton, toggleWave);
        debugButtonsContainer.setFocusTraversable(false);
        debugButtonsContainer.setPickOnBounds(false);
    }

    private void initStyles() {
        CssHelper.stilePulsanteMenu(debugButton);
        CssHelper.testoPrimario(debugButton);
        CssHelper.stilePulsanteMenu(toggleWave);
        CssHelper.testoPrimario(toggleWave);

        godModeLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 20));
        godModeLabel.setStyle("-fx-text-fill: #f1c40f; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");

        debugWarningLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 18));
        debugWarningLabel.setStyle("-fx-text-fill: #e74c3c; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");
    }

    private void initLayout() {
        getChildren().addAll(cheatLabelsContainer, debugToolBar, debugButtonsContainer);

        StackPane.setAlignment(debugToolBar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButtonsContainer, Pos.TOP_LEFT);

        cheatLabelsContainer.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(cheatLabelsContainer, Pos.BOTTOM_LEFT);
        cheatLabelsContainer.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);

        StackPane.setMargin(debugButtonsContainer, new Insets(50, 0, 0, 50));
        StackPane.setMargin(cheatLabelsContainer, new Insets(0, 0, 50, 50));
    }

    private void initBindings(Consumer<Boolean> onToggleDebugPanel) {
        debugToolBar.maxHeightProperty().bind(heightProperty().multiply(0.35));
        debugToolBar.maxWidthProperty().bind(widthProperty().multiply(ScalareAureo.IPHI_D));

        godModeLabel.visibleProperty().bind(gameController.godModeProperty());
        godModeLabel.managedProperty().bind(godModeLabel.visibleProperty());

        debugWarningLabel.setVisible(gameController.isDebugUsedInThisSession());
        debugWarningLabel.setManaged(debugWarningLabel.isVisible());

        gameController.debugModeProperty().addListener((obs, oldV, debugOn) -> {
            debugButtonsContainer.setVisible(debugOn);
            debugButtonsContainer.setManaged(debugOn);

            if (debugOn) {
                debugWarningLabel.setVisible(true);
                debugWarningLabel.setManaged(true);
            } else {
                debugToolBar.setVisible(false);
                debugToolBar.setManaged(false);
                debugPanelVisible = false;
                debugButton.setText("DEBUG");
                onToggleDebugPanel.accept(false);
            }
        });

        boolean initDebug = gameController.isDebugModeOn();
        debugButtonsContainer.setVisible(initDebug);
        debugButtonsContainer.setManaged(initDebug);
        debugToolBar.setVisible(false);
        debugToolBar.setManaged(false);
    }

    private void initEventHandlers(Consumer<Boolean> onToggleDebugPanel) {
        debugButton.setOnAction(ev -> {
            if (gameController.isDebugModeOn()) {
                debugPanelVisible = !debugPanelVisible;
                debugToolBar.setVisible(debugPanelVisible);
                debugToolBar.setManaged(debugPanelVisible);
                debugButton.setText(debugPanelVisible ? "HIDE DEBUG" : "DEBUG");

                debugWarningLabel.setVisible(true);
                debugWarningLabel.setManaged(true);

                onToggleDebugPanel.accept(debugPanelVisible);
            }
        });

        toggleWave.setOnAction(ev -> {
            if (gameController.isDebugModeOn()) {
                gameModel.waveActiveProperty().set(!gameModel.isWaveActive());
                toggleWave.setText(gameModel.isWaveActive() ? "pause wave" : "restart wave");
                onToggleDebugPanel.accept(debugPanelVisible);
            }
        });
    }

    /**
     * Sincronizza lo stato visivo della notifica di blocco salvataggi.
     */
    public void syncWarningState() {
        debugWarningLabel.setVisible(gameController.isDebugUsedInThisSession());
        debugWarningLabel.setManaged(debugWarningLabel.isVisible());
    }

    public boolean isDebugPanelVisible() {
        return debugPanelVisible;
    }
}