package uni.gaben.iscat;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.model.GameModel;
import uni.gaben.iscat.screens.game.view.GameView;
import uni.gaben.iscat.screens.bestiary.BestiaryView;
import uni.gaben.iscat.screens.login.LoginController;
import uni.gaben.iscat.screens.login.model.LoginAuth;
import uni.gaben.iscat.screens.login.model.LoginModel;
import uni.gaben.iscat.screens.login.LoginView;
import uni.gaben.iscat.screens.main_menu.MenuView;
import uni.gaben.iscat.screens.options.OptionsMenuView;
import uni.gaben.iscat.screens.scores.ScoreMenuView;
import uni.gaben.iscat.screens.skin_selection.SkinMenuView;
import uni.gaben.iscat.view.AbstractIscatStackPane;

public class IscatMVCRegistry {
    private IscatMVCRegistry() {
        /* mvc registry */
    }
    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU -> new LoginView(new LoginController(new LoginModel(), new LoginAuth(IscatDB.getInstance().getUsersQueries())));
            case MAIN_MENU -> new MenuView();
            case GAME ->  new GameView(new GameController(new GameModel()));
            case SKIN_MENU -> new SkinMenuView() ;
            case BESTIARY_MENU -> new BestiaryView();
            case SCORE_MENU -> new ScoreMenuView();
            case OPTIONS_MENU ->  new OptionsMenuView();
        };
    }
}
