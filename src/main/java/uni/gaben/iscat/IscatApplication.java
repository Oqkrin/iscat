package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.view.GameScene;

import uni.gaben.iscat.game.view.OptionsMenu;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.view.GamenexScene;
import uni.gaben.iscat.menus.bestiary_menu.BestiaryMenuController;
import uni.gaben.iscat.menus.bestiary_menu.BestiaryMenuScene;
import uni.gaben.iscat.menus.login_menu.controller.LoginController;
import uni.gaben.iscat.menus.login_menu.model.LoginData;
import uni.gaben.iscat.menus.login_menu.model.LoginModel;
import uni.gaben.iscat.menus.login_menu.view.LoginScene;
import uni.gaben.iscat.menus.main_menu.MenuController;
import uni.gaben.iscat.menus.main_menu.MenuScene;
import uni.gaben.iscat.menus.options_menu.OptionsMenuScene;
import uni.gaben.iscat.menus.score_menu.ScoreMenuController;
import uni.gaben.iscat.menus.score_menu.ScoreMenuScene;
import uni.gaben.iscat.menus.skin_menu.SkinMenuScene;
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

        // --- Scene map ---
        EnumMap<IscatScenes, Scene> scenes =  new EnumMap<>(IscatScenes.class);
        scenes.put(IscatScenes.LOGIN_MENU,    new LoginScene(loginModel, loginController));
        scenes.put(IscatScenes.MAIN_MENU,     new MenuScene(menuController));
        scenes.put(IscatScenes.GAME,          new GameScene(legacyController, legacyModel));
        scenes.put(IscatScenes.GAMEN,         new GamenexScene(gamenexController, gamenexModel));
        scenes.put(IscatScenes.SCORE_MENU,    new ScoreMenuScene());
        scenes.put(IscatScenes.SKIN_MENU,     new SkinMenuScene());
        //scenes.put(IscatScenes.OPTIONS_MENU,  new OptionsMenuScene());
        //scenes.put(IscatScenes.BESTIARY_MENU, new BestiaryMenuScene());
        //TODO OPTIONS MENU
        //TODO BESTIARY MENU

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
