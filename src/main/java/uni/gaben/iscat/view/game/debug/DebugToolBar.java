package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.utils.design.CssHelper;

/**
 * Pannello contenitore principale per gli strumenti di debug.
 * Gestisce la navigazione tra il menu principale delle opzioni, la console dei trucchi (Cheats)
 * e il generatore di entità (Spawner).
 */
public class DebugToolBar extends StackPane {

    /** Riferimento al controller principale del gioco. */
    private final GameController controller;

    /** Vista del menu principale della console di debug. */
    private VBox mainMenuView;

    /** Sotto-pannello dedicato all'attivazione dei trucchi (GodMode, GhostMode, ecc.). */
    private DebugToolBarCheats cheatsView;

    /** Sotto-pannello dedicato allo spawn visivo dei nemici e degli oggetti di gioco. */
    private DebugToolBarSpawner spawnerView;

    /**
     * Costruisce e inizializza la barra degli strumenti di debug.
     *
     * @param controller Il controller logico del gioco.
     */
    public DebugToolBar(GameController controller) {
        this.controller = controller;

        this.cheatsView = new DebugToolBarCheats(controller, this::showMainMenu);
        this.spawnerView = new DebugToolBarSpawner(controller, this::showMainMenu);

        buildMainMenu();
        applyStyles();
        showMainMenu();
    }

    /**
     * Mostra la schermata principale della console di debug.
     */
    public void showMainMenu() {
        getChildren().setAll(mainMenuView);
    }

    /**
     * Mostra il sotto-menu relativo ai Cheats.
     */
    public void showCheatsMenu() {
        getChildren().setAll(cheatsView);
    }

    /**
     * Mostra il sotto-menu relativo allo Spawner.
     */
    public void showSpawnerMenu() {
        getChildren().setAll(spawnerView);
    }

    /**
     * Costruisce il layout e i nodi del menu principale della console.
     */
    private void buildMainMenu() {
        mainMenuView = new VBox(20);
        mainMenuView.setAlignment(Pos.CENTER);
        mainMenuView.setPadding(new Insets(25));

        Label titleLabel = new Label("DEBUG CONSOLE");
        CssHelper.testoPrimario(titleLabel);
        CssHelper.labelLarge(titleLabel);
        titleLabel.setStyle(titleLabel.getStyle() + "; -fx-font-weight: bold; -fx-text-fill: #ffaa00; -fx-font-size: 16px;");

        HBox optionsContainer = new HBox(15);
        optionsContainer.setAlignment(Pos.CENTER);

        Button btnCheats = new Button("CHEATS");
        btnCheats.setPrefSize(130, 45);
        CssHelper.stilePulsanteMenu(btnCheats);
        CssHelper.testoPrimario(btnCheats);
        btnCheats.setOnAction(e -> showCheatsMenu());

        Button btnSpawn = new Button("SPAWN");
        btnSpawn.setPrefSize(130, 45);
        CssHelper.stilePulsanteMenu(btnSpawn);
        CssHelper.testoPrimario(btnSpawn);
        btnSpawn.setOnAction(e -> showSpawnerMenu());

        optionsContainer.getChildren().addAll(btnCheats, btnSpawn);
        mainMenuView.getChildren().addAll(titleLabel, optionsContainer);
    }

    /**
     * Applica le classi CSS e lo stile di sfondo alla barra di debug.
     */
    private void applyStyles() {
        getStyleClass().add("glowing-border");
        setStyle("-fx-background-color: #000000;");
    }
}