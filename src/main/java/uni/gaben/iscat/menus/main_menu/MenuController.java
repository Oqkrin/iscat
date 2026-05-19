package uni.gaben.iscat.menus.main_menu;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class MenuController {
    private StackPane contentRoot;

    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    private void navigate(IscatScenes target) {
        if (contentRoot != null) {
            IscatNavigator.getInstance().navigateWithFade(target, contentRoot);
        } else {
            IscatNavigator.getInstance().navigateTo(target);
        }
    }

    //public void playLegacy()      { navigate(IscatScenes.GAME);         }
    public void playPhi()         { navigate(IscatScenes.GAMEN);        }
    public void openSkinMenu()    { navigate(IscatScenes.SKIN_MENU);    }
    public void openOptionsMenu() { navigate(IscatScenes.OPTIONS_MENU); }
    public void openScoreMenu()   { navigate(IscatScenes.SCORE_MENU);   }
    public void openBestiaryMenu(){ navigate(IscatScenes.BESTIARY_MENU);}
    public void quit() {Platform.exit();}
}
