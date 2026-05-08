package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.view.GameCanvas;
import uni.gaben.iscat.game.view.GameScene;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.view.LoginScene;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.menu.view.MenuScene;
import uni.gaben.iscat.utils.IscatUtils;

import java.util.EnumMap;
import java.util.Objects;

/**
 * Radice della composizione MVC.
 * Costruisce e collega tutti i triad MVC.
 * Nessuna logica di business o navigazione qui - solo bootstrap.
 */
public class IscatApplication extends Application {

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
    }

    @Override
    public void start(Stage stage) {
        // Crea il model dell'applicazione
        IscatModel appModel = new IscatModel();

        // Costruisce i triad MVC
        LoginData loginData = LoginData.withDefaults();
        LoginModel loginModel = new LoginModel();
        LoginController loginController = new LoginController(loginModel, loginData);

        MenuController menuController = new MenuController();

        GameModel gameModel = new GameModel();
        GameCanvas gameCanvas = new GameCanvas(gameModel);
        GameController gameController = new GameController(gameModel, gameCanvas);

        // Costruisce la mappa delle scene
        EnumMap<IscatScenes, Scene> scenes = new EnumMap<>(IscatScenes.class);
        scenes.put(IscatScenes.LOGIN, new LoginScene(loginModel, loginController));
        scenes.put(IscatScenes.MENU, new MenuScene(menuController));
        scenes.put(IscatScenes.GAME, new GameScene(gameController, gameCanvas));

        // Applica il color theme globalmente a tutte le scene
        // I looked-up colors definiti su .root sono disponibili a tutti i discendenti
        String colorTheme = Objects.requireNonNull(IscatApplication.class
                        .getResource("/uni/gaben/iscat/styles/iscat-color-theme.css"))
                .toExternalForm();
        scenes.values().forEach(scene -> scene.getStylesheets().add(0, colorTheme));

        // Inizializza il sistema di navigazione
        IscatNavigator.getInstance().initialize(appModel, scenes);
        IscatController iscatController = new IscatController(appModel, stage, scenes);

        // Collega il comportamento finestra (drag, resize, pulsanti) a ogni scena
        scenes.values().forEach(scene -> {
            if (scene instanceof IscatSceneAbstract s) iscatController.wireScene(s);
        });

        // Setup stage
        stage.setTitle("ISCAT");
        stage.initStyle(StageStyle.UNDECORATED);

        iscatController.initializeScene();
        stage.show();
        IscatUtils.scalaCentraRispettoParent(stage, IscatUtils.getSchermiCorrenti(stage).get(0).getBounds());
    }

}
