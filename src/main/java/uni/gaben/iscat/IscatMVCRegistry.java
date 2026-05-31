package uni.gaben.iscat;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.sqlite.SQLiteScoreDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.model.GameModel;
import uni.gaben.iscat.screens.game.view.GameView;
import uni.gaben.iscat.screens.login.LoginController;
import uni.gaben.iscat.screens.login.model.LoginAuth;
import uni.gaben.iscat.screens.login.model.LoginModel;
import uni.gaben.iscat.screens.login.LoginView;
import uni.gaben.iscat.view.AbstractIscatStackPane;
import uni.gaben.iscat.view.GenericIscatView;

import java.sql.SQLException;

public class IscatMVCRegistry {
    private IscatMVCRegistry() {
        /* mvc registry */
    }

    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU -> new LoginView(new LoginController(new LoginModel(), new LoginAuth(IscatDB.getInstance().getUsersQueries())));
            case GAME -> {
                ScoreDAO scoreDAO = new SQLiteScoreDAO();  // Senza parametri
                GameController gameController = new GameController(new GameModel(), scoreDAO);
                yield new GameView(gameController);
            }
            case MAIN_MENU -> new GenericIscatView("/uni/gaben/iscat/fxml/main_menu.fxml");
            case SKIN_MENU -> new GenericIscatView("/uni/gaben/iscat/fxml/skin_menu.fxml");
            case BESTIARY_MENU -> new GenericIscatView("/uni/gaben/iscat/fxml/bestiary_menu.fxml");
            case SCORE_MENU -> new GenericIscatView("/uni/gaben/iscat/fxml/score_menu.fxml");
            case OPTIONS_MENU -> new GenericIscatView("/uni/gaben/iscat/fxml/options/options_menu.fxml");
        };
    }
}