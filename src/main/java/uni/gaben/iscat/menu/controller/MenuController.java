package uni.gaben.iscat.menu.controller;

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

    public void quit() {
        Platform.exit();
    }
}
