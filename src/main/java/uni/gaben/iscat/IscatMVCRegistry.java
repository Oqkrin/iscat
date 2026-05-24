package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.view.GameView;
import uni.gaben.iscat.menus.bestiary_menu.BestiaryView;
import uni.gaben.iscat.menus.login_menu.LoginController;
import uni.gaben.iscat.menus.login_menu.LoginData;
import uni.gaben.iscat.menus.login_menu.LoginModel;
import uni.gaben.iscat.menus.login_menu.LoginView;
import uni.gaben.iscat.menus.main_menu.MenuController;
import uni.gaben.iscat.menus.main_menu.MenuView;
import uni.gaben.iscat.menus.options_menu.OptionsMenuView;
import uni.gaben.iscat.menus.score_menu.ScoreMenuView;
import uni.gaben.iscat.menus.skin_menu.SkinMenuView;
import uni.gaben.iscat.view.AbstractIscatStackPane;

public class IscatMVCRegistry {
    private IscatMVCRegistry() {
        /* mvc registry */
    }
    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU -> new LoginView(new LoginController(new LoginModel(), LoginData.withPlaceholder()));
            case MAIN_MENU -> new MenuView(new MenuController());
            case GAME ->  new GameView(new GameController(new GameModel()));
            case SKIN_MENU ->  new ScoreMenuView();
            case BESTIARY_MENU -> new SkinMenuView();
            case SCORE_MENU -> new OptionsMenuView();
            case OPTIONS_MENU ->  new BestiaryView();
        };
    }
}
