package uni.gaben.iscat.controller;

import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.view.IscatWindow;

public class IscatWindowController {

    private final IscatModel model;
    private final IscatWindow window;

    public IscatWindowController(IscatModel model, Stage stage) {
        this.model = model;
        // The window sets itself as the scene root internally
        this.window = new IscatWindow(stage, stage.getScene());

        wireTitleBarActions(stage);
        syncModelWithWindow(stage);
    }

    public IscatWindow getWindow() { return window; }

    private void wireTitleBarActions(Stage stage) {
        var bar = window.getTitleBar();
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));
        bar.closeButton.setOnAction(e -> stage.close());
        bar.minimizeButton.setOnAction(e -> stage.setIconified(true));
        bar.maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        bar.fullscreenButton.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        bar.pinButton.setOnAction(e -> stage.setAlwaysOnTop(!stage.isAlwaysOnTop()));
    }

    private void syncModelWithWindow(Stage stage) {
        model.fullscreenProperty().addListener((obs, o, isFs) -> {
            if (stage.isFullScreen() != isFs) stage.setFullScreen(isFs);
        });
        stage.fullScreenProperty().addListener((obs, o, isFs) -> model.setFullscreen(isFs));

        // Pin state stays in model – bind to stage
        model.pinnedProperty().bind(stage.alwaysOnTopProperty());
    }
}