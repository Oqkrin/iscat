package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.utils.components.AbstractIscatStackPane;

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
    EnumMap<IscatScenes, AbstractIscatStackPane> scenes = new EnumMap<>(IscatScenes.class);

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
    public void initialize(IscatModel model) {
        this.model = model;
        for (IscatScenes scene : IscatScenes.values()) {
            scenes.put(scene, IscatMVCRegistry.getMVC(scene));
        }
    }

    /**
     * Naviga alla scena specificata.
     * Delega la gestione del ciclo di vita e della UI interamente a IscatController
     * tramite l'aggiornamento reattivo dello stato del Model.
     */
    public void navigateTo(IscatScenes targetScene) {
        if (model == null || scenes == null) {
            throw new IllegalStateException("IscatNavigator non inizializzato.");
        }

        // Questo cambierà la proprietà ascoltata da IscatController,
        // che farà il setAll() e gestirà le chiamate setActive()
        model.setCurrentScene(targetScene);
    }

    public AbstractIscatStackPane getScene(IscatScenes sceneType) {
        return scenes.get(sceneType);
    }

    public void navigateWithFade(IscatScenes target, StackPane currentContentRoot) {
        AbstractIscatStackPane nextView = scenes.get(target);
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