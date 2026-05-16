package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.view.GameSceneIscatScene;

import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.view.GamenexSceneIscatScene;
import uni.gaben.iscat.menus.bestiary_menu.BestiaryMenuSceneIscatScene;
import uni.gaben.iscat.menus.login_menu.controller.LoginController;
import uni.gaben.iscat.menus.login_menu.model.LoginData;
import uni.gaben.iscat.menus.login_menu.model.LoginModel;
import uni.gaben.iscat.menus.login_menu.view.LoginSceneIscatScene;
import uni.gaben.iscat.menus.main_menu.MenuController;
import uni.gaben.iscat.menus.main_menu.MenuSceneIscatScene;
import uni.gaben.iscat.menus.options_menu.OptionsMenuSceneIscatScene;
import uni.gaben.iscat.menus.score_menu.ScoreMenuSceneIscatScene;
import uni.gaben.iscat.menus.skin_menu.SkinMenuSceneIscatScene;

import java.util.EnumMap;
import java.util.Objects;

/**
 * Application root — pure bootstrap, no business logic.
 * Constructs and wires all MVC triads, then hands off to IscatNavigator.
 */
public class IscatApplication extends Application {

    IscatModel iscatModel = new IscatModel();

    // --- Login Menu ---
    LoginData       loginData       = LoginData.withDefaults();
    LoginModel      loginModel      = new LoginModel();
    LoginController loginController = new LoginController(loginModel, loginData);

    // --- Main Menu ---
    MenuController menuController = new MenuController();

    // --- Legacy game ---
    GameModel      legacyModel      = new GameModel();
    GameController legacyController = new GameController(legacyModel);

    // --- Game game ---
    GamenexModel      gamenexModel      = new GamenexModel();
    GamenexController gamenexController = new GamenexController(gamenexModel);

    EnumMap<IscatScenes, Scene> scenes =  new EnumMap<>(IscatScenes.class);

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);

        // --- Scene map ---
        putScenes();

        // Apply global colour theme to every scene
        String colorTheme = Objects.requireNonNull(
                        IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-color-theme.css"))
                .toExternalForm();
        scenes.values().forEach(s -> s.getStylesheets().addFirst(colorTheme));

        // Navigation
        IscatNavigator.getInstance().initialize(iscatModel, scenes);
    }

    private void putScenes() {
        scenes.put(IscatScenes.LOGIN_MENU,    new LoginSceneIscatScene(loginModel, loginController));
        scenes.put(IscatScenes.MAIN_MENU,     new MenuSceneIscatScene(menuController));
        scenes.put(IscatScenes.GAME,          new GameSceneIscatScene(legacyController, legacyModel));
        scenes.put(IscatScenes.GAMEN,         new GamenexSceneIscatScene(gamenexController, gamenexModel));
        scenes.put(IscatScenes.SCORE_MENU,    new ScoreMenuSceneIscatScene());
        scenes.put(IscatScenes.SKIN_MENU,     new SkinMenuSceneIscatScene());
        scenes.put(IscatScenes.OPTIONS_MENU,  new OptionsMenuSceneIscatScene());
        scenes.put(IscatScenes.BESTIARY_MENU, new BestiaryMenuSceneIscatScene());
    }

    @Override
    public void start(Stage stage) {
        IscatController iscatController = new IscatController(iscatModel, stage, scenes);
        // Wire window chrome (drag, resize, title-bar buttons) to every scene
        scenes.values().forEach(s -> {
            if (s instanceof AbstractIscatScene ais) iscatController.wireCustomDecoration(ais);
        });
        stage.setTitle("ISCAT");
        stage.initStyle(StageStyle.UNDECORATED);
        iscatController.initializeScene();
        stage.show();
        stage.centerOnScreen();
    }
}
