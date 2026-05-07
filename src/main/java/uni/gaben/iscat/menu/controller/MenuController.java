package uni.gaben.iscat.menu.controller;

import javafx.application.Platform;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.IscatNavigator;

public class MenuController {

    public void play() {
        IscatNavigator.getInstance().navigateTo(IscatScenes.GAME);
    }

    public void quit() {
        Platform.exit();
    }
}