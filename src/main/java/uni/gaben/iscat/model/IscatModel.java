package uni.gaben.iscat.model;

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
    private final BooleanProperty fullscreen = new SimpleBooleanProperty(false);

    public IscatViews getCurrentScene() { return currentScene.get(); }
    public TransitionType getPendingTransition() { return pendingTransition.get(); }

    public BooleanProperty fullscreenProperty() { return fullscreen; }
    public boolean isFullscreen() { return fullscreen.get(); }
    public void setFullscreen(boolean value) { this.fullscreen.set(value); }

    public void navigate(IscatViews scene, TransitionType type) {
        this.pendingTransition.set(type);
        this.currentScene.set(scene);
    }

    public ObjectProperty<IscatViews> currentSceneProperty() { return currentScene; }
    public boolean isPinned() { return pinned.get(); }
    public void setPinned(boolean value) { pinned.set(value); }
    public BooleanProperty pinnedProperty() { return pinned; }
}