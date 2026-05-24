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
import uni.gaben.iscat.controller.IscatViewController;
import uni.gaben.iscat.controller.IscatWindowController;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.view.IscatTitleBar;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.Objects;

/**
 * Application root — pure bootstrap, no business logic.
 * Constructs and wires all MVC triads, then hands off to IscatNavigator.
 */
public class IscatApplication extends Application {

    private final IscatModel iscatModel = new IscatModel();
    private final StackPane iscatApplicationRoot = new StackPane();
    private StackPane iscatContentRoot;

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        IscatNavigator.getInstance().initialize(iscatModel);
        AudioManager.getInstance().loadDefaultAudio();
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

        // Apply global rounded clips to the master root once
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

        // 1. Initialize Window Math/Decorations
        IscatWindowController windowController = new IscatWindowController(iscatModel, stage, iscatRootScene, iscatTitleBar);
        windowController.wireCustomDecoration();
        windowController.initializeWindow();

        // 2. Initialize the View Manager (Takes control of transitions)
        IscatViewController viewController = new IscatViewController(iscatModel, iscatContentRoot);

        // 3. Boot up the system explicitly to guarantee display on first layout pass
        viewController.showInitialView(IscatViews.LOGIN_MENU);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(iscatRootScene);
        stage.show();
        stage.centerOnScreen();
    }
}