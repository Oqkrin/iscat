package uni.gaben.iscat.controller;

import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.IscatMVCRegistry;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.IscatWindow;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import java.util.EnumMap;

/**
 * Controller centrale per la gestione, la transizione e lo scambio delle viste grafiche dell'applicazione.
 */
public class IscatViewController {

    private final StackPane view;
    private final IscatModel model;
    private final EnumMap<IscatViews, AbstractIscatStackPane> viewRegistry = new EnumMap<>(IscatViews.class);

    private static final java.util.Set<IscatViews> DYNAMIC_VIEWS = java.util.EnumSet.of(IscatViews.GAME);

    /**
     * Inizializza il controller delle viste precaricando quelle statiche e registrando il listener di navigazione.
     *
     * @param model  Il modello di navigazione globale.
     * @param window La finestra principale dell'applicazione.
     */
    public IscatViewController(IscatModel model, IscatWindow window) {
        this.model = model;
        this.view = window.getView();

        for (IscatViews scene : IscatViews.values()) {
            if (!DYNAMIC_VIEWS.contains(scene)) {
                viewRegistry.put(scene, IscatMVCRegistry.getMVC(scene));
            }
        }

        model.currentSceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != newScene) {
                performTransition(oldScene, newScene, model.getPendingTransition());
            }
        });
    }

    /**
     * Mostra la schermata iniziale impostando l'audio e l'interfaccia di partenza.
     *
     * @param initialView La vista iniziale da caricare.
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

    /**
     * Smista ed esegue il tipo di transizione richiesto verso la nuova schermata.
     *
     * @param oldScene La schermata di provenienza.
     * @param newScene La schermata di destinazione.
     * @param type     Il tipo di transizione da applicare.
     */
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

    /**
     * Esegue uno scambio immediato delle schermate senza transizioni visive.
     *
     * @param nextView La nuova vista da mostrare.
     */
    private void executeInstantSwap(AbstractIscatStackPane nextView) {
        view.getChildren().setAll(nextView);
        nextView.setOpacity(1.0);
        nextView.setActive(true);
    }

    /**
     * Configura ed esegue lo scambio delle schermate tramite effetto dissolvenza.
     *
     * @param oldScene La schermata precedente da rimuovere.
     * @param nextView La nuova vista da mostrare.
     */
    private void executeFadeSwap(IscatViews oldScene, AbstractIscatStackPane nextView) {
        AbstractIscatStackPane oldView = viewRegistry.get(oldScene);

        if (oldView != null && oldView.getParent() != null) {
            oldView.getParent().getScene().setFill(ThemeManager.getInstance().getBgPrimary());
            FadeTransition fadeOut = fade(nextView, oldView);
            fadeOut.play();
        } else {
            executeInstantSwap(nextView);
        }
    }

    /**
     * Crea l'animazione di FadeTransition per la vecchia vista gestendone la conclusione.
     *
     * @param nextView La nuova vista da mostrare al termine.
     * @param oldView  La vecchia vista in dissolvenza.
     * @return L'oggetto FadeTransition configurato.
     */
    private FadeTransition fade(AbstractIscatStackPane nextView, AbstractIscatStackPane oldView) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.20), oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            oldView.setActive(false);
            nextView.setOpacity(0.0);
            view.getChildren().setAll(nextView);
            nextView.setActive(true);
        });
        return fadeOut;
    }
}