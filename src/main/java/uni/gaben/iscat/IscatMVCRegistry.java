package uni.gaben.iscat;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.controller.LoginController;
import uni.gaben.iscat.model.login.LoginAuth;
import uni.gaben.iscat.model.login.LoginModel;
import uni.gaben.iscat.view.LoginView;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.IscatView;

public class IscatMVCRegistry {
    private IscatMVCRegistry() {
        /* mvc registry */
    }

    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU -> new LoginView(new LoginController(new LoginModel(), new LoginAuth(IscatDB.getInstance().getUserDAO())));
            case GAME -> {
                GameController gameController = new GameController(new GameModel());
                yield new GameView(gameController);
            }
            case MAIN_MENU -> new IscatView("/uni/gaben/iscat/fxml/MainMenu.fxml");
            case SKIN_MENU -> new IscatView("/uni/gaben/iscat/fxml/SkinMenu.fxml");
            case BESTIARY_MENU -> new IscatView("/uni/gaben/iscat/fxml/BestiaryMenu.fxml");
            case SCORE_MENU -> new IscatView("/uni/gaben/iscat/fxml/ScoreMenu.fxml");
            case SETTINGS_MENU -> new IscatView("/uni/gaben/iscat/fxml/SettingsMenu.fxml");
            case CREDITS -> new IscatView("/uni/gaben/iscat/fxml/CreditsMenu.fxml");
            case LEADERBOARD_MENU -> new IscatView("/uni/gaben/iscat/fxml/LeaderboardMenu.fxml");
            case TUTORIAL_MENU -> new IscatView("/uni/gaben/iscat/fxml/TutorialMenu.fxml");
        };
    }
}