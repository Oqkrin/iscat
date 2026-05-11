package uni.gaben.iscat;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Model dell'applicazione: stato globale condiviso.
 *
 * Contiene:
 * - scena corrente (navigazione)
 * - metadati di scena (BGM)
 * - stato finestra (drag, resize, pin, fullscreen bar)
 */
public class IscatModel {

    // -------------------------------------------------------------------------
    // Scene navigation
    // -------------------------------------------------------------------------

    private final ObjectProperty<IscatScenes> currentScene =
            new SimpleObjectProperty<>(IscatScenes.LOGIN);

    public IscatScenes getCurrentScene()                    { return currentScene.get(); }
    public void setCurrentScene(IscatScenes scene)          { currentScene.set(scene); }
    public ObjectProperty<IscatScenes> currentSceneProperty() { return currentScene; }

    /** BGM path for each scene — centralised so the controller stays audio-agnostic. */
    public String getBgmPath(IscatScenes scene) {
        return switch (scene) {
            case LOGIN    -> "/uni/gaben/iscat/audio/BGM/awesomeness.wav";
            case MENU     -> "/uni/gaben/iscat/audio/BGM/TremLoadingloopl.wav";
            case GAME,
                 GAMEN -> "/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav";
        };
    }

    // -------------------------------------------------------------------------
    // Window drag state
    // -------------------------------------------------------------------------

    double dragOffsetX = 0;
    double dragOffsetY = 0;

    // -------------------------------------------------------------------------
    // Window resize state
    // -------------------------------------------------------------------------

    enum ResizeDir { NONE, N, S, E, W, NE, NW, SE, SW }

    ResizeDir resizeDir = ResizeDir.NONE;
    double resizeStartX, resizeStartY;
    double resizeStartW, resizeStartH;
    double resizeStartStageX, resizeStartStageY;

    // -------------------------------------------------------------------------
    // Window decoration state
    // -------------------------------------------------------------------------

    /** Whether the title bar is currently visible (used during fullscreen). */
    boolean barVisible = true;

    /** Whether the window is pinned always-on-top. */
    private final BooleanProperty pinned = new SimpleBooleanProperty(false);

    public boolean isPinned()                          { return pinned.get(); }
    public void setPinned(boolean value)               { pinned.set(value); }
    public BooleanProperty pinnedProperty()            { return pinned; }
}
