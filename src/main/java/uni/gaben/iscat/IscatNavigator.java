package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;

/**
 * Pure intent dispatcher.
 * No UI nodes, no animations, no views. Just tells the Model where to go.
 */
public class IscatNavigator {
    private static IscatNavigator instance;
    private IscatModel model;

    private IscatNavigator() {}

    public static IscatNavigator getInstance() {
        if (instance == null) { instance = new IscatNavigator(); }
        return instance;
    }

    public void initialize(IscatModel model) {
        this.model = model;
    }

    public void navigateTo(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.INSTANT);
    }

    public void navigateWithFade(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.FADE);
    }
}