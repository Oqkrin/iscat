package uni.gaben.iscat;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.EnumMap;

/**
 * Controller dell'applicazione: gestisce Stage, Scene e transizioni.
 * Osserva IscatModel e reagisce ai cambiamenti di scena.
 */
public class IscatController {
    private final IscatModel model;
    private final Stage stage;
    private final EnumMap<IscatScenes, Scene> sceneMap;

    public IscatController(IscatModel model, Stage stage, EnumMap<IscatScenes, Scene> sceneMap) {
        this.model = model;
        this.stage = stage;
        this.sceneMap = sceneMap;

        // Osserva il model e reagisce ai cambiamenti di scena
        model.currentSceneProperty().addListener((obs, oldScene, newScene) -> {
            performSceneTransition(newScene);
        });
    }

    private void performSceneTransition(IscatScenes scene) {
        // Cambia BGM in base alla scena
        String bgmPath = switch (scene) {
            case LOGIN -> "/uni/gaben/iscat/audio/BGM/awesomeness.wav";
            case MENU  -> "/uni/gaben/iscat/audio/BGM/TremLoadingloopl.wav";
            case GAME  -> "/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav";
        };

        IscatAudioManager.getInstance().playBGM(bgmPath, true);
        stage.setScene(sceneMap.get(scene));
    }

    /**
     * Inizializza la scena iniziale (chiamato dopo start()).
     */
    public void initializeScene() {
        performSceneTransition(model.getCurrentScene());
    }
}
