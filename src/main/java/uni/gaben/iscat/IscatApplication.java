package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.view.GameScene;

import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.view.GamenexScene;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.view.LoginScene;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.menu.view.MenuScene;
import uni.gaben.iscat.skin_menu.SkinMenuScene;
import uni.gaben.iscat.utils.IscatUtils;

import java.util.EnumMap;
import java.util.Objects;

/**
 * Application root — pure bootstrap, no business logic.
 * Constructs and wires all MVC triads, then hands off to IscatNavigator.
 */
public class IscatApplication extends Application {

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
    }

    @Override
    public void start(Stage stage) {
        IscatModel appModel = new IscatModel();

        // --- Login ---
        LoginData       loginData       = LoginData.withDefaults();
        LoginModel      loginModel      = new LoginModel();
        LoginController loginController = new LoginController(loginModel, loginData);

        // --- Menu ---
        MenuController menuController = new MenuController();

        // --- Legacy game ---
        GameModel      legacyModel      = new GameModel();
        GameController legacyController = new GameController(legacyModel);

        // --- Game game ---
        GamenexModel      gamenexModel      = new GamenexModel();
        GamenexController gamenexController = new GamenexController(gamenexModel);

        // --- Scene map ---
        EnumMap<IscatScenes, Scene> scenes = new EnumMap<>(IscatScenes.class);
        scenes.put(IscatScenes.LOGIN,    new LoginScene(loginModel, loginController));
        scenes.put(IscatScenes.MENU,     new MenuScene(menuController));
        scenes.put(IscatScenes.GAME,     new GameScene(legacyController, legacyModel));
        scenes.put(IscatScenes.GAMEN, new GamenexScene(gamenexController, gamenexModel));
        scenes.put(IscatScenes.SKIN_MENU, new SkinMenuScene());
        // Apply global colour theme to every scene
        String colorTheme = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-color-theme.css"))
                .toExternalForm();
        scenes.values().forEach(s -> s.getStylesheets().add(0, colorTheme));

        // Navigation
        IscatNavigator.getInstance().initialize(appModel, scenes);
        IscatController iscatController = new IscatController(appModel, stage, scenes);

        // Wire window chrome (drag, resize, title-bar buttons) to every scene
        scenes.values().forEach(s -> {
            if (s instanceof IscatSceneAbstract abs) iscatController.wireScene(abs);
        });

        stage.setTitle("ISCAT");
        stage.initStyle(StageStyle.UNDECORATED);
        iscatController.initializeScene();
        stage.show();
        IscatUtils.scalaCentraRispettoParent(stage,
                IscatUtils.getSchermiCorrenti(stage).get(0).getBounds());
    }
}
