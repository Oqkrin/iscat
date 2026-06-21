package uni.gaben.iscat.controller;

import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.view.IscatWindow;

/**
 * Controller responsabile della gestione della finestra principale dell'applicazione.
 * Configura le azioni della barra del titolo personalizzata e sincronizza lo stato bidirezionale
 */
public class IscatWindowController {

    private final IscatModel model;
    private final IscatWindow window;

    public IscatWindowController(IscatModel model, Stage stage) {
        this.model = model;
        // La finestra si imposta internamente come radice della scena
        this.window = new IscatWindow(stage, stage.getScene());

        wireTitleBarActions(stage);
        syncModelWithWindow(stage);
    }

    /** Restituisce l'istanza della finestra controllata. */
    public IscatWindow getWindow() { return window; }

    /** Associa i componenti grafici della barra del titolo nativa/custom alle rispettive azioni dello Stage. */
    private void wireTitleBarActions(Stage stage) {
        var bar = window.getTitleBar();
        // Disabilita o sovrascrive la combinazione standard di uscita dal fullscreen inserendo "DELETE"
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));
        bar.closeButton.setOnAction(e -> stage.close());
        bar.minimizeButton.setOnAction(e -> stage.setIconified(true));
        bar.maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        bar.fullscreenButton.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        bar.pinButton.setOnAction(e -> stage.setAlwaysOnTop(!stage.isAlwaysOnTop()));
    }

    /** Sincronizza lo stato delle proprietà dello Stage con le proprietà reattive dell'IscatModel. */
    private void syncModelWithWindow(Stage stage) {
        // Sincronizzazione bidirezionale del flag Fullscreen tra modello e Stage
        model.fullscreenProperty().addListener((obs, o, isFs) -> {
            if (stage.isFullScreen() != isFs) stage.setFullScreen(isFs);
        });
        stage.fullScreenProperty().addListener((obs, o, isFs) -> model.setFullscreen(isFs));

        // Lega la proprietà 'pinned' del modello allo stato 'alwaysOnTop' della finestra
        model.pinnedProperty().bind(stage.alwaysOnTopProperty());
    }
}