package uni.gaben.iscat.iscat_model_vc;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Model dell'applicazione: stato globale condiviso.
 */
public class IscatModel {

    public enum TransitionType { INSTANT, FADE }

    private final ObjectProperty<IscatViews> currentScene = new SimpleObjectProperty<>(IscatViews.LOGIN_MENU);
    private final ObjectProperty<TransitionType> pendingTransition = new SimpleObjectProperty<>(TransitionType.INSTANT);
    private final BooleanProperty pinned = new SimpleBooleanProperty(false);

    public IscatViews getCurrentScene() { return currentScene.get(); }
    public TransitionType getPendingTransition() { return pendingTransition.get(); }

    public void navigate(IscatViews scene, TransitionType type) {
        this.pendingTransition.set(type);
        this.currentScene.set(scene);
    }

    public ObjectProperty<IscatViews> currentSceneProperty() { return currentScene; }

    public String getBgmPath(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU    -> "/uni/gaben/iscat/audio/BGM/awesomeness.wav";
            case MAIN_MENU,
                 SKIN_MENU,
                 BESTIARY_MENU,
                 SCORE_MENU,
                 OPTIONS_MENU -> "/uni/gaben/iscat/audio/BGM/TremLoadingloopl.wav";
            case GAME          -> "/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav";
        };
    }

    public boolean isPinned() { return pinned.get(); }
    public void setPinned(boolean value) { pinned.set(value); }
    public BooleanProperty pinnedProperty() { return pinned; }
}