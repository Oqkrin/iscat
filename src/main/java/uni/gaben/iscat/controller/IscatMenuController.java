package uni.gaben.iscat.controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;

/**
 * Estensione di IscatFxmlController per i controller di menu che:
 * - hanno bisogno di reagire a ESC
 * - devono registrare/deregistrare listener sulla scena correttamente
 */
public interface IscatMenuController extends IscatFxmlController {
    /**
     * Restituisce il nodo radice del menu, usato per agganciare
     * il listener sceneProperty().
     */
    Pane getRootPane();

    /**
     * Azione da eseguire quando si preme ESC (o si clicca back).
     * Di default naviga al main menu; override per comportamento custom.
     */
    default void handleBack() { IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU); }

    /**
     * Registra il listener ESC sulla scena in modo sicuro,
     * rimuovendolo quando la scena cambia o il nodo viene rimosso.
     * Da chiamare in initialize().
     */
    default void registerEscHandler() {
        // riferimento stabile alla lambda, necessario per removeEventFilter
        EventHandler<KeyEvent>[] handler = new EventHandler[1];
        handler[0] = e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                handleBack();
                e.consume();
            }
        };

        getRootPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, handler[0]);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, handler[0]);
            }
        });
    }
}