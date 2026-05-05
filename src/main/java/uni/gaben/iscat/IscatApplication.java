package uni.gaben.iscat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uni.gaben.iscat.game.view.GamePanel;
import uni.gaben.iscat.game.view.GameScene;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.view.LoginScene;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.menu.view.MenuScene;
import uni.gaben.iscat.utils.IscatUtils;

import javafx.scene.text.Font;

import java.util.EnumMap;
import java.util.Map;

public class IscatApplication extends Application {

    private static final Map<String, String> UTENTI = Map.of(
        "gaben", "iscat"
    );


    private LoginData       loginData;
    private LoginModel      loginModel;
    private LoginController loginController;
    private MenuController menuController;
    private GamePanel gamePanel = new GamePanel();


    private EnumMap<IscatScenes, Scene> iscatScenes = new EnumMap<>(IscatScenes.class);
    private Stage stage;

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);

        loginData       = new LoginData(UTENTI);
        loginModel      = new LoginModel();
        loginController = new LoginController(loginModel, loginData);
        loginController.setOnLoginSuccess(() -> setScene(IscatScenes.MENU));

        menuController = new MenuController();
        menuController.setOnMenuStartGame(() -> setScene(IscatScenes.GAME));
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        iscatScenes.put(IscatScenes.LOGIN, new LoginScene(loginModel, loginController));
        iscatScenes.put(IscatScenes.MENU, new MenuScene(menuController));
        iscatScenes.put(IscatScenes.GAME, new GameScene(gamePanel));

        stage.setTitle("ISCAT");
        setScene(IscatScenes.LOGIN);
        stage.show();
        IscatUtils.scalaCentraRispettoParent(stage, IscatUtils.getSchermiCorrenti(stage).get(0).getBounds());
    }

    private void setScene(IscatScenes scene) {
        stage.setScene(iscatScenes.get(scene));
        if(scene == IscatScenes.GAME){
            gamePanel.startGameThread();
        }
    }
}

