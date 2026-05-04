package uni.gaben.iscat.menu.controller;

import javafx.application.Platform;

public class MenuController {
    private Runnable onMenuStartGame;

    public void play() {
        if (onMenuStartGame != null) {
            onMenuStartGame.run();
        }
    }

    public void setOnMenuStartGame(Runnable onMenuStartGame) {
        this.onMenuStartGame = onMenuStartGame;
    }

    public void quit() {
        Platform.exit();
    }
}