package uni.gaben.iscat.controller;

import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.IscatMVCRegistry;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.view.AbstractIscatStackPane;
import java.util.EnumMap;

/**
 * Solely responsible for swapping nodes, playing transitions,
 * and managing view lifecycles within the single Stage.
 */
public class IscatViewController {

    private final StackPane contentRoot;
    private final IscatModel model;
    private final EnumMap<IscatViews, AbstractIscatStackPane> viewRegistry = new EnumMap<>(IscatViews.class);

    public IscatViewController(IscatModel model, StackPane contentRoot) {
        this.model = model;
        this.contentRoot = contentRoot;

        // 1. Initialize all views here
        for (IscatViews scene : IscatViews.values()) {
            viewRegistry.put(scene, IscatMVCRegistry.getMVC(scene));
        }

        // 2. Listen for subsequent navigation intents
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
            AudioManager.getInstance().playBGM(model.getBgmPath(initialView), true);
            executeInstantSwap(firstView);
        }
    }

    private void performTransition(IscatViews oldScene, IscatViews newScene, IscatModel.TransitionType type) {
        AbstractIscatStackPane nextView = viewRegistry.get(newScene);
        if (nextView == null) return;

        nextView.initialize();
        AudioManager.getInstance().playBGM(model.getBgmPath(newScene), true);

        if (type == IscatModel.TransitionType.FADE && !contentRoot.getChildren().isEmpty()) {
            executeFadeSwap(oldScene, nextView);
        } else {
            if (oldScene != null && viewRegistry.containsKey(oldScene)) {
                viewRegistry.get(oldScene).setActive(false);
            }
            executeInstantSwap(nextView);
        }
    }

    private void executeInstantSwap(AbstractIscatStackPane nextView) {
        contentRoot.getChildren().setAll(nextView);
        nextView.setOpacity(1.0);
        nextView.setActive(true);
    }

    private void executeFadeSwap(IscatViews oldScene, AbstractIscatStackPane nextView) {
        AbstractIscatStackPane oldView = viewRegistry.get(oldScene);

        if (oldView != null) {
            // Fade out the old view instance directly
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.25), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                oldView.setActive(false);

                // Set the incoming view completely transparent before attachment
                nextView.setOpacity(0.0);
                contentRoot.getChildren().setAll(nextView);
                nextView.setActive(true); // Internally launches structural load and its own fadeIn animation
            });
            fadeOut.play();
        } else {
            executeInstantSwap(nextView);
        }
    }
}