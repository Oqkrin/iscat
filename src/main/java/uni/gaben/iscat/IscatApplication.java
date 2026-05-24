package uni.gaben.iscat;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import uni.gaben.iscat.game.controller.GameController;

import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.universe.UniverseController;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.view.GameView;
import uni.gaben.iscat.game.view.camera.CameraModel;
import uni.gaben.iscat.menus.bestiary_menu.BestiaryView;
import uni.gaben.iscat.menus.login_menu.LoginController;
import uni.gaben.iscat.menus.login_menu.LoginData;
import uni.gaben.iscat.menus.login_menu.LoginModel;
import uni.gaben.iscat.menus.login_menu.LoginView;
import uni.gaben.iscat.menus.main_menu.MenuController;
import uni.gaben.iscat.menus.main_menu.MenuView;
import uni.gaben.iscat.menus.options_menu.OptionsMenuView;
import uni.gaben.iscat.menus.score_menu.ScoreMenuView;
import uni.gaben.iscat.menus.skin_menu.SkinMenuView;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.EnumMap;
import java.util.Objects;

/**
 * Application root — pure bootstrap, no business logic.
 * Constructs and wires all MVC triads, then hands off to IscatNavigator.
 */
public class IscatApplication extends Application {

    IscatModel iscatModel = new IscatModel();


    private final StackPane iscatApplicationRoot = new StackPane();
    private StackPane iscatContentRoot; // The new dynamic inner container

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        IscatNavigator.getInstance().initialize(iscatModel);
        IscatAudioManager.getInstance().loadDefaultAudio();
        ThemeColors.ensureLoaded();
    }

    @Override
    public void start(Stage stage) {
        Scene iscatRootScene = new Scene(iscatApplicationRoot);
        iscatRootScene.setFill(ThemeColors.parsedColors.get("bg-primary"));

        IscatTitleBar iscatTitleBar = new IscatTitleBar();
        iscatTitleBar.setMaxHeight(Region.USE_PREF_SIZE);

        Region iscatWindowBorderOverlay = new Region();
        iscatWindowBorderOverlay.getStyleClass().add("window-border-overlay");
        iscatWindowBorderOverlay.setMouseTransparent(true);
        StackPane.setAlignment(iscatTitleBar, Pos.TOP_CENTER);

        iscatContentRoot = new StackPane();

        iscatApplicationRoot.getChildren().addAll(iscatContentRoot, iscatTitleBar, iscatWindowBorderOverlay);

        // 3. Apply global rounded clips to the master root once
        Rectangle clip = new Rectangle();
        clip.setArcWidth(32.0);
        clip.setArcHeight(32.0);
        clip.widthProperty().bind(iscatApplicationRoot.widthProperty());
        clip.heightProperty().bind(iscatApplicationRoot.heightProperty());
        iscatApplicationRoot.setClip(clip);

        String colorTheme = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-color-theme.css")).toExternalForm();
        String typography = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-typography.css")).toExternalForm();
        String components = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-components-shared.css")).toExternalForm();

        iscatRootScene.getStylesheets().addAll(colorTheme, typography, components);

        IscatController iscatController = new IscatController(iscatModel, stage, iscatRootScene, iscatContentRoot, iscatTitleBar);

        iscatController.wireCustomDecoration();

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(iscatRootScene);
        iscatController.initializeScene();
        stage.show();
        stage.centerOnScreen();
    }
}
