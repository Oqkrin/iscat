package uni.gaben.iscat;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Model dell'applicazione: stato globale e navigazione.
 * Contiene lo stato corrente della scena e (in futuro) sessione utente.
 */
public class IscatModel {
    private static final ObjectProperty<IscatScenes> currentScene = new SimpleObjectProperty<>(IscatScenes.LOGIN);

    public IscatScenes getCurrentScene() {
        return currentScene.get();
    }

    public ObjectProperty<IscatScenes> currentSceneProperty() {
        return currentScene;
    }

    public static void setCurrentScene(IscatScenes scene) {
        currentScene.set(scene);
    }
}
