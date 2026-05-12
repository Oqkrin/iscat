package uni.gaben.iscat.menus.main_menu;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatSceneAbstract;

/**
 * Main menu screen.
 * Offers two play buttons: one for the legacy game package,
 * one for the new game_phi architecture.
 */
public class MenuScene extends IscatSceneAbstract {

    private static final String CSS_MENU_BUTTON = "menu-button";
    private static final String CSS_TITLE       = "menu-title";

    private final MenuController controller;

    private StackPane root;
    private Label     title;
    private Button    playLegacyButton;
    private Button    playPhiButton;
    private Button    optionsButton;
    private Button    scoreButton;
    private Button    skinButton;
    private Button    bestiaryButton;
    private Button    quitButton;

    public MenuScene(MenuController menuController) {
        super(new StackPane(), true); // starry background
        this.controller = menuController;
        this.root = getContentRoot();
        root.setStyle("-fx-background-color: transparent;");
        initialize();
    }

    @Override
    protected void initStyles() {
        String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        getStylesheets().add(css);
    }

    @Override
    protected void initNodes() {
        title = new Label("ISCAT");
        title.getStyleClass().add(CSS_TITLE);

        // --- Play Legacy ---
        FontIcon legacyIcon = new FontIcon("fas-play");
        legacyIcon.setIconSize(32);
        playLegacyButton = new Button("PLAY  (legacy)", legacyIcon);
        playLegacyButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Play Phi ---
        FontIcon phiIcon = new FontIcon("fas-rocket");
        phiIcon.setIconSize(32);
        playPhiButton = new Button("PLAY  (game_phi)", phiIcon);
        playPhiButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Options (placeholder) ---
        FontIcon optionIcon = new FontIcon("fas-cog");
        optionIcon.setIconSize(32);
        optionsButton = new Button("OPTIONS", optionIcon);
        optionsButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Score (placeholder) ---
        FontIcon scoreIcon = new FontIcon("fas-eye");
        scoreIcon.setIconSize(32);
        scoreButton = new Button("VIEW SCORE", scoreIcon);
        scoreButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Skin Button ---
        FontIcon skinIcon = new FontIcon("fas-eye");
        skinIcon.setIconSize(32);
        skinButton = new Button("CHANGE SKIN", scoreIcon);
        skinButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Skin Button ---
        FontIcon bestiaryIcon = new FontIcon("fas-eye");
        bestiaryIcon.setIconSize(32);
        bestiaryButton = new Button("BESTIARIO", scoreIcon);
        bestiaryButton.getStyleClass().add(CSS_MENU_BUTTON);

        // --- Quit ---
        FontIcon quitIcon = new FontIcon("fas-door-open");
        quitIcon.setIconSize(32);
        quitButton = new Button("QUIT", quitIcon);
        quitButton.getStyleClass().add(CSS_MENU_BUTTON);
    }

    @Override
    protected void initLayout() {
        // Two play buttons side-by-side
        HBox playRow = new HBox(16, playLegacyButton, playPhiButton);
        playRow.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, title, playRow, optionsButton, scoreButton, skinButton, bestiaryButton, quitButton);
        layout.setAlignment(Pos.CENTER);

        root.getChildren().add(layout);
    }

    @Override
    protected void initBindings() {}

    @Override
    protected void initEventHandlers() {
        playLegacyButton.setOnAction(e -> controller.playLegacy());
        playPhiButton.setOnAction(e    -> controller.playPhi());
        quitButton.setOnAction(e       -> controller.quit());
        skinButton.setOnAction(e -> controller.openSkinMenu());
        optionsButton.setOnAction(e -> controller.openOptionsMenu());
        scoreButton.setOnAction(e -> controller.openScoreMenu());
        bestiaryButton.setOnAction(e -> controller.openBestiaryMenu());

    }

    @Override
    public void onShow() {
        if (getStarryBackground() != null) {
            getStarryBackground().setFollowMouse(true);
            setOnMouseMoved(e -> getStarryBackground().updateMousePosition(e.getSceneX(), e.getSceneY()));
        }
    }

    @Override
    public void onHide() {
        setOnMouseMoved(null);
    }
}
