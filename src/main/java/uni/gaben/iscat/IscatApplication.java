package uni.gaben.iscat;

import javafx.application.Application;
import javafx.stage.Stage;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.view.LoginScene;
import uni.gaben.iscat.utils.IscatUtils;

import javafx.scene.text.Font;

import java.util.Map;

public class IscatApplication extends Application {

    private static final Map<String, String> UTENTI = Map.of(
        "gaben", "iscat"
    );

    private LoginData       loginData;
    private LoginModel      loginModel;
    private LoginController loginController;

    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        loginData       = new LoginData(UTENTI);
        loginModel      = new LoginModel();
        loginController = new LoginController(loginModel, loginData);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ISCAT");
        stage.setScene(new LoginScene(loginModel, loginController));
        stage.show();
        IscatUtils.scalaCentraRispettoParent(stage, IscatUtils.getSchermiCorrenti(stage).get(0).getBounds());
    }
}
