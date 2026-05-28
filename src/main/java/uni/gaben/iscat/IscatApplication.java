package uni.gaben.iscat;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.iscat_mv_controller.IscatViewController;
import uni.gaben.iscat.iscat_mv_controller.IscatWindowController;
import uni.gaben.iscat.iscat_model_vc.IscatModel;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.iscat_m_view_c.IscatTitleBar;
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

    private final StackPane iscatApplicationRoot = new StackPane();
    private final Scene iscatRootScene = new Scene(iscatApplicationRoot);
    private final StackPane iscatContentRoot = new StackPane();
    private final IscatTitleBar iscatTitleBar = new IscatTitleBar();
    private final Region iscatWindowBorderOverlay = new Region();
    private final IscatDB db = IscatDB.getInstance();
    private IscatWindowController iscatWindowController;
    private IscatViewController iscatViewController;


    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        db.init();
        IscatNavigator.getInstance().initialize(iscatModel);
        AudioManager.getInstance().loadDefaultAudio();
    }

    @Override
    public void start(Stage stage) {
        iscatRootScene.setFill(ThemeManager.getInstance().getBgPrimary());
        iscatTitleBar.setMaxHeight(56.0);

        iscatRootScene.widthProperty().addListener((observable, oldValue, newValue) -> {

            printNodeInfo(iscatRootScene.getRoot(), 0);
        });

        //this might fix scalings
        iscatContentRoot.prefWidthProperty().bind(iscatApplicationRoot.widthProperty());
        iscatContentRoot.prefHeightProperty().bind(iscatApplicationRoot.heightProperty());


        iscatWindowBorderOverlay.getStyleClass().add("window-border");
        iscatWindowBorderOverlay.setMouseTransparent(true);
        StackPane.setAlignment(iscatTitleBar, Pos.TOP_CENTER);

        iscatApplicationRoot.getChildren().addAll(iscatContentRoot, iscatTitleBar, iscatWindowBorderOverlay);

        IscatUtils.roundRectangle(iscatApplicationRoot, IscatSettings.BORDER_RADIUS);
        addIscatStyles(iscatRootScene);

        iscatWindowController = new IscatWindowController(iscatModel, stage, iscatRootScene, iscatTitleBar);
        iscatViewController = new IscatViewController(iscatModel, iscatContentRoot);
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


    void printNodeInfo(Node node, int depth) {
        if (node == null) return;

        // Indent for readability
        String indent = "  ".repeat(depth);

        System.out.println(indent + node.getClass().getSimpleName() +
                " | Bounds: " + node.getBoundsInParent());

        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                printNodeInfo(child, depth + 1);
            }
        }
    }

}