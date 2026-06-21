package uni.gaben.iscat.controller;

import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.IscatMVCRegistry;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.IscatWindow;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import java.util.EnumMap;

/**
 * Controller responsabile dello scambio dei nodi della UI, della gestione delle transizioni
 * visive (fade/istantanee) e del ciclo di vita delle schermate (view) all'interno dell'unico Stage.
 */
public class IscatViewController {

    private final StackPane view;
    private final IscatModel model;
    private final EnumMap<IscatViews, AbstractIscatStackPane> viewRegistry = new EnumMap<>(IscatViews.class);

    /** Insieme delle viste dinamiche che richiedono la rigenerazione del modulo MVC a ogni accesso. */
    private static final java.util.Set<IscatViews> DYNAMIC_VIEWS = java.util.EnumSet.of(IscatViews.GAME);

    public IscatViewController(IscatModel model, IscatWindow window) {
        this.model = model;
        this.view = window.getView();

        // Inizializza e registra preventivamente tutte le view statiche
        for (IscatViews scene : IscatViews.values()) {
            if (!DYNAMIC_VIEWS.contains(scene)) {
                viewRegistry.put(scene, IscatMVCRegistry.getMVC(scene));
            }
        }

        // Resta in ascolto dei cambiamenti della proprietà della scena corrente per gestire la navigazione
        model.currentSceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != newScene) {
                performTransition(oldScene, newScene, model.getPendingTransition());
            }
        });
    }

    /**
     * Safely boots up the very first screen without requiring a property state-change trigger.
     */
    public void showInitialView(IscatViews initialView) {
        model.navigate(initialView, IscatModel.TransitionType.INSTANT);
        AbstractIscatStackPane firstView = viewRegistry.get(initialView);
        if (firstView != null) {
            firstView.initialize();
            AudioManager.getInstance().playBGM(AudioManager.getBgmPath(initialView), true);
            executeInstantSwap(firstView);
        }
    }

    private void performTransition(IscatViews oldScene, IscatViews newScene, IscatModel.TransitionType type) {
        if (DYNAMIC_VIEWS.contains(newScene)) {
            viewRegistry.put(newScene, IscatMVCRegistry.getMVC(newScene));
        }
        AbstractIscatStackPane nextView = viewRegistry.get(newScene);
        if (nextView == null) return;

        nextView.initialize();
        AudioManager.getInstance().playBGM(AudioManager.getBgmPath(newScene), true);

        if (type == IscatModel.TransitionType.FADE && !view.getChildren().isEmpty()) {
            executeFadeSwap(oldScene, nextView);
        } else {
            if (oldScene != null && viewRegistry.containsKey(oldScene)) {
                viewRegistry.get(oldScene).setActive(false);
            }
            executeInstantSwap(nextView);
        }
    }

    private void executeInstantSwap(AbstractIscatStackPane nextView) {
        view.getChildren().setAll(nextView);
        nextView.setOpacity(1.0);
        nextView.setActive(true);
    }

    private void executeFadeSwap(IscatViews oldScene, AbstractIscatStackPane nextView) {
        AbstractIscatStackPane oldView = viewRegistry.get(oldScene);

        if (oldView != null) {
            oldView.getParent().getScene().setFill(ThemeManager.getInstance().getBgPrimary());
            FadeTransition fadeOut = fade(nextView, oldView);
            fadeOut.play();
        } else {
            executeInstantSwap(nextView);
        }
    }

    private FadeTransition fade(AbstractIscatStackPane nextView, AbstractIscatStackPane oldView) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.20), oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            oldView.setActive(false);

            nextView.setOpacity(0.0);
            view.getChildren().setAll(nextView);
            nextView.setActive(true); // Internally launches structural load and its own fadeIn animation
        });
        return fadeOut;
    }
}