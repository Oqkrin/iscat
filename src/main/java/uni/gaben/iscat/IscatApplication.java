package uni.gaben.iscat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.controller.IscatViewController;
import uni.gaben.iscat.controller.IscatWindowController;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.view.components.IscatTitleBar;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.IscatUtils;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Objects;

/**
 * Application root — pure bootstrap, no business logic.
 * Constructs and wires all MVC triads, then hands off to IscatNavigator.
 */
public class IscatApplication extends Application {

    private final IscatModel iscatModel = new IscatModel();
    private final StackPane iscatRootPane = new StackPane();
    private final Scene iscatRootScene = new Scene(iscatRootPane);
    private final StackPane iscatPointerToContent = new StackPane();
    private final IscatTitleBar iscatTitleBar = new IscatTitleBar();
    private final Region iscatWindowBorderOverlay = new Region();

    IscatViewController iscatViewController;
    IscatWindowController iscatWindowController;

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        IscatDB.getInstance().init();
        IscatNavigator.getInstance().initialize(iscatModel);
    }

    @Override
    public void start(Stage stage) {


        AudioManager.getInstance().loadAllSFX("/uni/gaben/iscat/audio/SFX/entitiesSFX");
        AudioManager.getInstance().loadDefaultAudio();


        iscatRootScene.setFill(ThemeManager.getInstance().getBgPrimary());
        iscatTitleBar.setMaxHeight(56.0);

        //this might fix scalings
        iscatPointerToContent.prefWidthProperty().bind(iscatRootPane.widthProperty());
        iscatPointerToContent.prefHeightProperty().bind(iscatRootPane.heightProperty());


        iscatWindowBorderOverlay.getStyleClass().add("window-border");
        iscatWindowBorderOverlay.setMouseTransparent(true);
        StackPane.setAlignment(iscatTitleBar, Pos.TOP_CENTER);

        iscatRootPane.getChildren().addAll(iscatPointerToContent, iscatTitleBar, iscatWindowBorderOverlay);

        IscatUtils.roundRectangle(iscatRootPane, IscatSettings.BORDER_RADIUS);
        addIscatStyles(iscatRootScene);

        iscatWindowBorderOverlay.visibleProperty().bind(iscatModel.fullscreenProperty().not());

        iscatWindowController = new IscatWindowController(iscatModel, stage, iscatRootScene, iscatTitleBar);
        iscatViewController = new IscatViewController(iscatModel, iscatPointerToContent);
        iscatViewController.showInitialView(IscatViews.LOGIN_MENU);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(iscatRootScene);
        stage.show();
        stage.centerOnScreen();
    }

    private void addIscatStyles(Scene scene) {
        String colorTheme = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-color-theme.css")).toExternalForm();
        String typography = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-typography.css")).toExternalForm();
        String components = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-components-shared.css")).toExternalForm();
        scene.getStylesheets().addAll(colorTheme, typography, components);
    }

    @Override
    public void stop() throws Exception {
        IscatDB.getInstance().shutdown();
    }

}