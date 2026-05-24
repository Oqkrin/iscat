package uni.gaben.iscat;

import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.iscat_screens.game.controller.GameController;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;
import uni.gaben.iscat.iscat_screens.game.view.GameView;
import uni.gaben.iscat.iscat_screens.bestiary.BestiaryView;
import uni.gaben.iscat.iscat_screens.login.LoginController;
import uni.gaben.iscat.iscat_screens.login.LoginData;
import uni.gaben.iscat.iscat_screens.login.LoginModel;
import uni.gaben.iscat.iscat_screens.login.LoginView;
import uni.gaben.iscat.iscat_screens.main_menu.MenuController;
import uni.gaben.iscat.iscat_screens.main_menu.MenuView;
import uni.gaben.iscat.iscat_screens.options.OptionsMenuView;
import uni.gaben.iscat.iscat_screens.scores.ScoreMenuView;
import uni.gaben.iscat.iscat_screens.skin_selection.SkinMenuView;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

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
