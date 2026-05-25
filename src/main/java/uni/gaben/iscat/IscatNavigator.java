package uni.gaben.iscat;

import uni.gaben.iscat.iscat_model_vc.IscatModel;
import uni.gaben.iscat.iscat_model_vc.IscatViews;

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

    public void navigateInstantTo(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.INSTANT);
    }

    public void navigateWithFade(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.FADE);
    }
}