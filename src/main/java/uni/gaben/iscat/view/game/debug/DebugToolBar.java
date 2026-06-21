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

    /**
     * Costruisce la barra degli strumenti di debug.
     * Inizializza le viste secondarie per trucchi e spawner, crea il menu principale e applica gli stili.
     *
     * @param controller Il controller logico del gioco.
     */
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

    /** Mostra all'interno del pannello la vista del menu principale di debug. */
    public void showMainMenu() {
        getChildren().setAll(mainMenuView);
    }

    /** Mostra all'interno del pannello l'interfaccia dedicata all'attivazione dei trucchi (Cheats). */
    public void showCheatsMenu() {
        getChildren().setAll(cheatsView);
    }

    /** Mostra all'interno del pannello l'interfaccia dedicata alla generazione forzata delle entità (Spawner). */
    public void showSpawnerMenu() {
        getChildren().setAll(spawnerView);
    }

    /** Costruisce graficamente la struttura del menu principale, configurandone pulsanti, spaziature ed eventi. */
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

    /** Assegna le classi CSS alla barra di debug e carica il rispettivo foglio di stile esterno. */
    private void applyStyles() {
        getStyleClass().addAll("debug-tool-bar", "glowing-border");
        String cssPath = getClass().getResource("/uni/gaben/iscat/styles/screens/game/debug-tool-bar.css").toExternalForm();
        getStylesheets().add(cssPath);
    }
}