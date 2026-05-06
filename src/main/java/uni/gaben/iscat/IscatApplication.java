package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.model.entities.GameModel;
import uni.gaben.iscat.game.view.GameCanvas;
import uni.gaben.iscat.game.view.GameScene;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.view.LoginScene;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.menu.view.MenuScene;
import uni.gaben.iscat.utils.IscatUtils;
import uni.gaben.iscat.utils.audio_manager.AudioManager;

import java.util.EnumMap;

/**
 * Radice della composizione MVC.
 * Costruisce e collega tutti i triad MVC; gestisce le transizioni di scena.
 * Nessuna logica di business qui.
 */
public class IscatApplication extends Application {

    private LoginModel      loginModel;
    private LoginController loginController;
    private MenuController  menuController;
    private GameModel       gameModel;
    private GameCanvas      gameCanvas;
    private GameController  gameController;

    private final EnumMap<IscatScenes, Scene> scenes = new EnumMap<>(IscatScenes.class);
    private Stage stage;

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);

        LoginData loginData = LoginData.withDefaults();
        loginModel      = new LoginModel();
        loginController = new LoginController(loginModel, loginData);
        loginController.setOnLoginSuccess(() -> setScene(IscatScenes.MENU));

        menuController = new MenuController();
        menuController.setOnMenuStartGame(() -> setScene(IscatScenes.GAME));

        gameModel      = new GameModel();
        gameCanvas     = new GameCanvas(gameModel);
        gameController = new GameController(gameModel, gameCanvas);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        scenes.put(IscatScenes.LOGIN, new LoginScene(loginModel, loginController));
        scenes.put(IscatScenes.MENU,  new MenuScene(menuController));
        scenes.put(IscatScenes.GAME,  new GameScene(gameController, gameCanvas));

        stage.setTitle("ISCAT");
        setScene(IscatScenes.LOGIN);
        stage.show();
        IscatUtils.scalaCentraRispettoParent(stage, IscatUtils.getSchermiCorrenti(stage).get(0).getBounds());
    }

    private void setScene(IscatScenes scene) {
        // Troviamo la bgm giusta per la scena che verrà settata
        String bgm_path = switch (scene) {
            case LOGIN -> "/uni/gaben/iscat/audio/BGM/awesomeness.wav";
            case MENU  -> "/uni/gaben/iscat/audio/BGM/TremLoadingloopl.wav";
            case GAME  -> "/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav";
        };

        // Cambiamo musica quando settiamo la nuova scena
        AudioManager.getInstance().playBGM(bgm_path,true);
        stage.setScene(scenes.get(scene));
    }
}
