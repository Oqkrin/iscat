package uni.gaben.iscat.iscat_screens.main_menu;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

import java.util.Objects;

/**
 * Main menu screen.
 * Offers two play buttons: one for the legacy game package,
 * one for the new game_phi architecture.
 */
public class MenuView extends AbstractIscatStackPane {

    private static final String CSS_MENU_BUTTON = "menu-button";
    private static final String CSS_TITLE       = "menu-title";
    private static final String CSS_MENU_ROOT   = "menu-root";

    private final MenuController controller;

    private Label     title;
    private Button    playButton;
    private Button    optionsButton;
    private Button    scoreButton;
    private Button    skinButton;
    private Button    bestiaryButton;
    private Button    logoutButton;
    private Button    quitButton;

    public MenuView(MenuController menuController) {
        super(new StackPane(), true);
        this.controller = menuController;
        StackPane contentRoot = getContentRoot();
        this.controller.setContentRoot(contentRoot);
        contentRoot.getStyleClass().add(CSS_MENU_ROOT);
        initialize();
    }

    @Override
    protected void initStyles() {
        String css = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/scenes/main-menu.css")).toExternalForm();
        getStylesheets().add(css);
    }

    @Override
    protected void initNodes() {
        title = new Label("ISCAT");
        title.getStyleClass().add(CSS_TITLE);

        playButton     = createMenuButton("PLAY", "fas-rocket");
        optionsButton  = createMenuButton("OPTIONS", "fas-cog");
        scoreButton    = createMenuButton("VIEW SCORE", "fas-eye");
        skinButton     = createMenuButton("CHANGE SKIN", "fas-gift");
        bestiaryButton = createMenuButton("BESTIARIO", "fas-bug");
        logoutButton   = createMenuButton("LOG OUT", "fas-door-open");
        quitButton     = createMenuButton("QUIT", "fas-door-open");
    }

    @Override
    protected void initLayout() {
        VBox layout = new VBox(20, title, playButton, optionsButton, scoreButton, skinButton, bestiaryButton,logoutButton, quitButton);
        layout.setAlignment(Pos.CENTER);
        getContentRoot().getChildren().add(layout);
    }

    @Override
    protected void initEventHandlers() {
        playButton.setOnAction(e     -> controller.playGame());
        optionsButton.setOnAction(e  -> controller.openOptionsMenu());
        scoreButton.setOnAction(e    -> controller.openScoreMenu());
        skinButton.setOnAction(e     -> controller.openSkinMenu());
        bestiaryButton.setOnAction(e -> controller.openBestiaryMenu());
        logoutButton.setOnAction(e   -> controller.logout());
        quitButton.setOnAction(e     -> controller.quit());
    }

    private Button createMenuButton(String text, String iconCode) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(32);
        Button btn = new Button(text, icon);
        btn.getStyleClass().add(CSS_MENU_BUTTON);
        btn.setFocusTraversable(false);
        return btn;
    }
}
