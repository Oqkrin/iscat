package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.utils.design.CssHelper;

/**
 * Pannello contenitore principale per gli strumenti di debug.
 * Gestisce la navigazione tra il menu principale delle opzioni, la console dei trucchi (Cheats)
 * e il generatore di entità (Spawner).
 */
public class DebugToolBar extends StackPane {

    private VBox mainMenuView;
    private final DebugToolBarCheats cheatsView;
    private final DebugToolBarSpawner spawnerView;

    public DebugToolBar(GameController controller) {
        final double SU = IscatSettings.STANDARD_UNIT;

        setPickOnBounds(false);
        setFocusTraversable(false);

        this.cheatsView = new DebugToolBarCheats(controller, this::showMainMenu);
        this.spawnerView = new DebugToolBarSpawner(controller, this::showMainMenu);

        buildMainMenu();
        applyStyles();
        showMainMenu();
    }

    public void showMainMenu() {
        getChildren().setAll(mainMenuView);
    }

    public void showCheatsMenu() {
        getChildren().setAll(cheatsView);
    }

    public void showSpawnerMenu() {
        getChildren().setAll(spawnerView);
    }

    private void buildMainMenu() {
        final double SU = IscatSettings.STANDARD_UNIT;

        mainMenuView = new VBox(SU /2); // 8
        mainMenuView.setAlignment(Pos.CENTER);
        mainMenuView.setPadding(new Insets(SU /2));
        mainMenuView.setPickOnBounds(false);
        mainMenuView.setFocusTraversable(false);

        Label titleLabel = new Label("DEBUG CONSOLE");
        titleLabel.setFocusTraversable(false);
        titleLabel.setMouseTransparent(true);
        CssHelper.testoPrimario(titleLabel);
        CssHelper.labelLarge(titleLabel);
        titleLabel.getStyleClass().add("debug-title");
        titleLabel.setPadding(new Insets(SU /4)); // 4

        HBox optionsContainer = new HBox(SU /2);
        optionsContainer.setAlignment(Pos.CENTER);
        optionsContainer.setPickOnBounds(false);
        optionsContainer.setFocusTraversable(false);

        Button btnCheats = new Button("CHEATS");
        btnCheats.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnCheats);
        CssHelper.testoPrimario(btnCheats);
        btnCheats.setPadding(new Insets(SU /4, SU /2, SU /4, SU /2)); // 4,8,4,8
        btnCheats.setOnAction(e -> showCheatsMenu());

        Button btnSpawn = new Button("SPAWN");
        btnSpawn.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnSpawn);
        CssHelper.testoPrimario(btnSpawn);
        btnSpawn.setPadding(new Insets(SU /4, SU /2, SU /4, SU/2));
        btnSpawn.setOnAction(e -> showSpawnerMenu());

        optionsContainer.getChildren().addAll(btnCheats, btnSpawn);
        mainMenuView.getChildren().addAll(titleLabel, optionsContainer);
    }

    private void applyStyles() {
        getStyleClass().addAll("debug-tool-bar", "glowing-border");
        String cssPath = getClass().getResource("/uni/gaben/iscat/styles/screens/game/debug-tool-bar.css").toExternalForm();
        getStylesheets().add(cssPath);
    }
}