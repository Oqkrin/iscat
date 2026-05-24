package uni.gaben.iscat.iscat_screens.main_menu;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_model_vc.IscatViews;

public class MenuController {
    private StackPane contentRoot;

    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    private void navigate(IscatViews scene) {
        IscatNavigator.getInstance().navigateWithFade(scene);
    }

    public void playGame()          { navigate(IscatViews.GAME);          }
    public void openSkinMenu()      { navigate(IscatViews.SKIN_MENU);     }
    public void openOptionsMenu()   { navigate(IscatViews.OPTIONS_MENU);  }
    public void openScoreMenu()     { navigate(IscatViews.SCORE_MENU);    }
    public void openBestiaryMenu()  { navigate(IscatViews.BESTIARY_MENU); }
    public void logout()            { navigate(IscatViews.LOGIN_MENU);    }
    public void quit()              { Platform.exit();                     }
}
