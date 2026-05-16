package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.EnumMap;

/**
 * Singleton facade per la navigazione.
 * Altri controller chiamano questo per cambiare scena senza conoscere Stage/Scene.
 * ARCHITECTURE:
 * - IscatModel: contiene lo stato corrente (quale scena è attiva)
 * - IscatNavigator: gestisce la navigazione e il ciclo di vita delle scene
 * - IscatController: osserva il model e aggiorna lo Stage
 */
public class IscatNavigator {
    private static IscatNavigator instance;
    private IscatModel model;
    private EnumMap<IscatScenes, Scene> sceneMap;

    private IscatNavigator() {}

    public static IscatNavigator getInstance() {
        if (instance == null) {
            instance = new IscatNavigator();
        }
        return instance;
    }

    /**
     * Inizializza il navigator con il model e la mappa delle scene.
     * Chiamato da IscatApplication durante il bootstrap.
     */
    public void initialize(IscatModel model, EnumMap<IscatScenes, Scene> sceneMap) {
        this.model = model;
        this.sceneMap = sceneMap;
    }

    /**
     * Naviga alla scena specificata.
     * Gestisce il ciclo di vita delle scene se implementano IscatSceneLifecycleInterface.
     */
    public void navigateTo(IscatScenes targetScene) {
        if (model == null || sceneMap == null) {
            throw new IllegalStateException("IscatNavigator non inizializzato. Chiamare initialize() prima.");
        }
        
        IscatScenes currentScene = model.getCurrentScene();
        
        // Nascondi scena corrente
        if (currentScene != null && currentScene != targetScene) {
            Scene scene = sceneMap.get(currentScene);
            if (scene instanceof IscatSceneLifecycleInterface lifecycleScene) {
                lifecycleScene.setActive(false);
            }
        }
        
        // Mostra nuova scena (il model trigger l'observer in IscatController)
        model.setCurrentScene(targetScene);
        
        // Attiva la nuova scena
        Scene scene = sceneMap.get(targetScene);
        if (scene instanceof IscatSceneLifecycleInterface lifecycleScene) {
            lifecycleScene.setActive(true);
        }
    }

    public AbstractIscatScene getScene(IscatScenes sceneType) {
        Scene scene = sceneMap.get(sceneType);
        return scene instanceof AbstractIscatScene ? (AbstractIscatScene) scene : null;
    }

    public void navigateWithFade(IscatScenes target, StackPane currentContentRoot) {
        // 1. Recupera il contentRoot della scena di destinazione e portalo a 0 subito
        StackPane targetContentRoot = null;
        Scene targetScene = sceneMap.get(target);
        if (targetScene instanceof AbstractIscatScene nextScene) {
            targetContentRoot = nextScene.getContentRoot();
            if (targetContentRoot != null) targetContentRoot.setOpacity(0.0);
        }

        final StackPane finalTarget = targetContentRoot;

        // 2. Fade-out della scena corrente
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), currentContentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            navigateTo(target);
            currentContentRoot.setOpacity(1.0); // resetta per la prossima volta

            // 3. Fade-in della nuova scena
            if (finalTarget != null) {
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), finalTarget);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
        fadeOut.play();
    }
}
