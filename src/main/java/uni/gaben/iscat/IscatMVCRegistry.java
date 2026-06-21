package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.view.LoginView;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.IscatFXMLView;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registro centrale per l'accoppiamento e la creazione dei componenti MVC dell'applicazione.
 * Mappa ogni visualizzazione (IscatViews) al rispettivo fornitore (Supplier) in grado di istanziare
 * la vista corretta, sia essa basata su codice custom o su layout FXML standard.
 */
public final class IscatMVCRegistry {

    private static final Map<IscatViews, Supplier<AbstractIscatStackPane>> REGISTRY = new EnumMap<>(IscatViews.class);

    static {
        // Registrazione viste personalizzate (Custom)
        REGISTRY.put(IscatViews.LOGIN_MENU, LoginView::new);
        REGISTRY.put(IscatViews.GAME, () -> new GameView(new GameController(new GameModel())));

        // Registrazione viste FXML standard
        registerFxml(IscatViews.MAIN_MENU, "MainMenu.fxml");
        registerFxml(IscatViews.SKIN_MENU, "SkinMenu.fxml");
        registerFxml(IscatViews.BESTIARY_MENU, "BestiaryMenu.fxml");
        registerFxml(IscatViews.SCORE_MENU, "ScoreMenu.fxml");
        registerFxml(IscatViews.SETTINGS_MENU, "SettingsMenu.fxml");
        registerFxml(IscatViews.CREDITS, "CreditsMenu.fxml");
        registerFxml(IscatViews.LEADERBOARD_MENU, "LeaderboardMenu.fxml");
        registerFxml(IscatViews.TUTORIAL_MENU, "TutorialMenu.fxml");
        registerFxml(IscatViews.ENTITY_EDITOR, "EntityEditorMenu.fxml");
    }

    /** Costruttore privato per impedire l'istanza di una classe di utility. */
    private IscatMVCRegistry() {
    }

    /**
     * Associa una costante di visualizzazione a un file FXML standard all'interno del registro.
     *
     * @param view     La costante della vista da registrare
     * @param fxmlName Il nome del file FXML (inclusa estensione) presente nella cartella delle risorse
     */
    private static void registerFxml(IscatViews view, String fxmlName) {
        REGISTRY.put(view, () -> new IscatFXMLView("/uni/gaben/iscat/fxml/" + fxmlName));
    }

    /**
     * Recupera e istanzia un nuovo blocco di componenti visivi per la schermata richiesta.
     *
     * @param scene La costante identificativa della schermata desiderata
     * @return Una nuova istanza del pannello grafico corrispondente alla vista
     * @throws IllegalArgumentException Se non è stata registrata alcuna factory per la costante specificata
     */
    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        Supplier<AbstractIscatStackPane> factory = REGISTRY.get(scene);
        if (factory == null) {
            throw new IllegalArgumentException("Nessuna View registrata nel sistema per la scena: " + scene);
        }
        return factory.get();
    }
}