package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.utils.design.CssHelper;

public class DebugToolBar extends StackPane {

    private final GameController controller;

    private VBox mainMenuView;
    private DebugToolBarCheats cheatsView;
    private DebugToolBarSpawner spawnerView;
    private Button closeButton;

    public DebugToolBar(GameController controller) {
        this.controller = controller;

        this.cheatsView = new DebugToolBarCheats(controller, this::showMainMenu);
        this.spawnerView = new DebugToolBarSpawner(controller, this::showMainMenu);

        buildCloseButton();
        buildMainMenu();
        applyStyles();
        showMainMenu();
    }

    private void buildCloseButton() {
        closeButton = new Button("X");
        closeButton.setFocusTraversable(false);
        closeButton.setPrefSize(30, 30);
        closeButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        closeButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        closeButton.setAlignment(Pos.CENTER);
        closeButton.getStyleClass().add("glowing-border");

        closeButton.setStyle(
                "-fx-background-color: #000000; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Miracode'; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-radius: 1px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0; " +
                        "-fx-content-display: TEXT_ONLY;"
        );

        closeButton.setOnMouseEntered(e -> closeButton.setStyle(closeButton.getStyle() + "-fx-background-color: #222222;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle(closeButton.getStyle() + "-fx-background-color: #000000;"));

        closeButton.setOnAction(e -> {
            setVisible(false);
            setManaged(false);
        });

        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(10));
    }

    public void showMainMenu() {
        getChildren().setAll(mainMenuView, closeButton);
    }

    public void showCheatsMenu() {
        getChildren().setAll(cheatsView, closeButton);
    }

    public void showSpawnerMenu() {
        getChildren().setAll(spawnerView, closeButton);
    }

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

    private void applyStyles() {
        getStyleClass().add("glowing-border");
        setStyle("-fx-background-color: #000000;");
    }
}