package uni.gaben.iscat;

import javafx.animation.FadeTransition;
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
    private EnumMap<IscatScenes, AbstractIscatStackPane> viewMap;

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
    public void initialize(IscatModel model, EnumMap<IscatScenes, AbstractIscatStackPane> viewMap) {
        this.model = model;
        this.viewMap = viewMap;
    }

    /**
     * Naviga alla scena specificata.
     * Gestisce il ciclo di vita delle scene se implementano IscatSceneLifecycleInterface.
     */
    public void navigateTo(IscatScenes targetScene) {
        if (model == null || viewMap == null) {
            throw new IllegalStateException("IscatNavigator non inizializzato.");
        }

        IscatScenes currentScene = model.getCurrentScene();

        if (currentScene != null && currentScene != targetScene) {
            AbstractIscatStackPane view = viewMap.get(currentScene);
            if (view != null) {
                view.setActive(false);
            }
        }

        // Questo cambierà la proprietà ascoltata da IscatController, che farà il setAll()
        model.setCurrentScene(targetScene);

        AbstractIscatStackPane nextView = viewMap.get(targetScene);
        if (nextView != null) {
            nextView.setActive(true);
        }
    }

    public AbstractIscatStackPane getScene(IscatScenes sceneType) {
        return viewMap.get(sceneType);
    }

    public void navigateWithFade(IscatScenes target, StackPane currentContentRoot) {
        AbstractIscatStackPane nextView = viewMap.get(target);
        StackPane targetContentRoot = (nextView != null) ? nextView.getContentRoot() : null;

        if (targetContentRoot != null) {
            targetContentRoot.setOpacity(0.0);
        }

        final StackPane finalTarget = targetContentRoot;

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), currentContentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            navigateTo(target);
            currentContentRoot.setOpacity(1.0);

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
