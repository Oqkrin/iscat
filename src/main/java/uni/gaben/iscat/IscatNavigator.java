package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;

/**
 * Disegnatore e distributore puro degli intenti di navigazione (Dispatcher).
 * Nessun nodo UI, nessuna animazione, nessuna vista. Si occupa esclusivamente di indicare al Modello la destinazione.
 */
public class IscatNavigator {
    private static IscatNavigator instance;
    private IscatModel model;

    /** Costruttore privato per implementare il pattern Singleton. */
    private IscatNavigator() {}

    /**
     * Restituisce l'istanza unica (Singleton) del navigatore.
     *
     * @return L'istanza di IscatNavigator.
     */
    public static IscatNavigator getInstance() {
        if (instance == null) { instance = new IscatNavigator(); }
        return instance;
    }

    /**
     * Restituisce il modello globale dell'applicazione associato al navigatore.
     *
     * @return Il modello IscatModel.
     */
    public IscatModel getModel() { return model; }

    /**
     * Inizializza il navigatore associandovi il modello logico dell'applicazione.
     *
     * @param model Il modello globale da controllare.
     */
    public void initialize(IscatModel model) {
        this.model = model;
    }

    /**
     * Cambia la vista corrente impostando la destinazione in modo immediato, senza transizioni visive.
     *
     * @param targetScene La costante della vista di destinazione.
     */
    public void navigateInstantTo(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.INSTANT);
    }

    /**
     * Cambia la vista corrente applicando una transizione visiva di dissolvenza (Fade).
     *
     * @param targetScene La costante della vista di destinazione.
     */
    public void navigateWithFade(IscatViews targetScene) {
        model.navigate(targetScene, IscatModel.TransitionType.FADE);
    }
}