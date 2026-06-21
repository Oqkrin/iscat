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

import java.util.Objects;

/**
 * Pannello contenitore principale per gli strumenti di debug.
 * Gestisce la navigazione tra il menu principale delle opzioni, la console dei trucchi (Cheats)
 * e il generatore di entità (Spawner).
 */
public class DebugToolBar extends StackPane {

    /** Vista del menu principale della console di debug. */
    private VBox mainMenuView;

    /** Sotto-pannello dedicato all'attivazione dei trucchi (GodMode, GhostMode, ecc.). */
    private final DebugToolBarCheats cheatsView;

    /** Sotto-pannello dedicato allo spawn visivo dei nemici e degli oggetti di gioco. */
    private final DebugToolBarSpawner spawnerView;

    /**
     * Costruisce e inizializza la barra degli strumenti di debug.
     *
     * @param controller Il controller logico del gioco.
     */
    public DebugToolBar(GameController controller) {
        setPickOnBounds(false);
        setFocusTraversable(false);

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
        mainMenuView = new VBox(8);
        mainMenuView.setAlignment(Pos.CENTER);
        mainMenuView.setPadding(new Insets(8, 8, 8, 8));
        mainMenuView.setPickOnBounds(false);
        mainMenuView.setFocusTraversable(false);

        Label titleLabel = new Label("DEBUG CONSOLE");
        titleLabel.setFocusTraversable(false);
        titleLabel.setMouseTransparent(true);
        CssHelper.testoPrimario(titleLabel);
        CssHelper.labelLarge(titleLabel);
        titleLabel.getStyleClass().add("debug-title");

        HBox optionsContainer = new HBox(8);
        optionsContainer.setAlignment(Pos.CENTER);
        optionsContainer.setPickOnBounds(false);
        optionsContainer.setFocusTraversable(false);

        Button btnCheats = new Button("CHEATS");
        btnCheats.setPrefSize(64, 32);
        btnCheats.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnCheats);
        CssHelper.testoPrimario(btnCheats);
        btnCheats.setOnAction(e -> showCheatsMenu());

        Button btnSpawn = new Button("SPAWN");
        btnSpawn.setPrefSize(64, 32);
        btnSpawn.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnSpawn);
        CssHelper.testoPrimario(btnSpawn);
        btnSpawn.setOnAction(e -> showSpawnerMenu());

        optionsContainer.getChildren().addAll(btnCheats, btnSpawn);
        mainMenuView.getChildren().addAll(titleLabel, optionsContainer);
    }

    /**
     * Applica le classi CSS alla barra di debug.
     */
    private void applyStyles() {
        getStyleClass().addAll("debug-tool-bar", "glowing-border");
        String cssPath = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/screens/game/debug-tool-bar.css")).toExternalForm();
        getStylesheets().add(cssPath);
    }
}