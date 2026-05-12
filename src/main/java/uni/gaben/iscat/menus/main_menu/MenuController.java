package uni.gaben.iscat.menus.main_menu;

import javafx.application.Platform;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class MenuController {

    /** Launch the legacy game (game package). */
    public void playLegacy() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.GAME);
    }

    /** Launch the new gamenex architecture. */
    public void playPhi() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.GAMEN);
    }

    /** Open the PlayerSkinChooseMenu */
    public void openSkinMenu() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.SKIN_MENU);
    }

    /** Apre il menu delle opzioni **/
    public void openOptionsMenu() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.OPTIONS_MENU);
    }

    /** Apre il menu dello score **/
    public void openScoreMenu() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.SCORE_MENU);
    }

    /** Apre il menu del bestiary **/
    public void openBestiaryMenu() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.BESTIARY_MENU);
    }

    public void quit() {
        Platform.exit();
    }
}
